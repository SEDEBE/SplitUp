-- SplitUp - MySQL Schema (Local) - CORREGIDO
-- Autor: Alejandro Córdoba Pérez
-- Nota: 'groups' es palabra reservada en MySQL -> usamos 'expense_groups'

DROP DATABASE IF EXISTS splitup;
CREATE DATABASE splitup
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE splitup;

-- =========================
-- USERS
-- =========================
CREATE TABLE users (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  display_name VARCHAR(120) NOT NULL,
  email VARCHAR(190) NOT NULL,
  avatar_url VARCHAR(512) NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB;

-- =========================
-- AUTH IDENTITIES (OAuth)
-- =========================
CREATE TABLE auth_identities (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT UNSIGNED NOT NULL,

  provider ENUM('LOCAL','GOOGLE','FACEBOOK') NOT NULL,
  provider_user_id VARCHAR(191) NOT NULL,
  email_at_provider VARCHAR(190) NULL,

  -- Recomendación: NO guardar tokens en texto plano.
  access_token_hash VARCHAR(255) NULL,
  refresh_token_hash VARCHAR(255) NULL,
  token_expires_at DATETIME NULL,

  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_auth_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE CASCADE,

  UNIQUE KEY uk_provider_user (provider, provider_user_id),
  KEY idx_auth_user (user_id)
) ENGINE=InnoDB;

-- =========================
-- GROUPS & MEMBERS
-- =========================
CREATE TABLE expense_groups (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(120) NOT NULL,
  description TEXT NULL,
  created_by BIGINT UNSIGNED NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT fk_groups_created_by FOREIGN KEY (created_by)
    REFERENCES users(id),

  KEY idx_groups_created_by (created_by)
) ENGINE=InnoDB;

CREATE TABLE group_members (
  group_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,
  role ENUM('OWNER','ADMIN','MEMBER') NOT NULL DEFAULT 'MEMBER',
  joined_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (group_id, user_id),

  CONSTRAINT fk_gm_group FOREIGN KEY (group_id)
    REFERENCES expense_groups(id) ON DELETE CASCADE,

  CONSTRAINT fk_gm_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE CASCADE,

  KEY idx_gm_user (user_id)
) ENGINE=InnoDB;

-- =========================
-- CATEGORIES (opcional)
-- =========================
CREATE TABLE categories (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(80) NOT NULL,
  icon VARCHAR(80) NULL,
  UNIQUE KEY uk_categories_name (name)
) ENGINE=InnoDB;

-- =========================
-- EXPENSES
-- =========================
CREATE TABLE expenses (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  group_id BIGINT UNSIGNED NOT NULL,
  payer_user_id BIGINT UNSIGNED NOT NULL,
  category_id BIGINT UNSIGNED NULL,

  title VARCHAR(140) NOT NULL,
  total_amount DECIMAL(12,2) NOT NULL,
  currency CHAR(3) NOT NULL DEFAULT 'EUR',
  expense_date DATE NOT NULL,
  note TEXT NULL,

  split_mode ENUM('EQUAL','CUSTOM') NOT NULL DEFAULT 'EQUAL',

  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT fk_exp_group FOREIGN KEY (group_id)
    REFERENCES expense_groups(id) ON DELETE CASCADE,

  CONSTRAINT fk_exp_payer FOREIGN KEY (payer_user_id)
    REFERENCES users(id),

  CONSTRAINT fk_exp_category FOREIGN KEY (category_id)
    REFERENCES categories(id),

  KEY idx_exp_group (group_id),
  KEY idx_exp_payer (payer_user_id),
  KEY idx_exp_date (expense_date)
) ENGINE=InnoDB;

-- =========================
-- EXPENSE SHARES
-- =========================
CREATE TABLE expense_shares (
  expense_id BIGINT UNSIGNED NOT NULL,
  user_id BIGINT UNSIGNED NOT NULL,

  share_type ENUM('EQUAL','CUSTOM') NOT NULL DEFAULT 'EQUAL',
  amount_assigned DECIMAL(12,2) NULL,

  PRIMARY KEY (expense_id, user_id),

  CONSTRAINT fk_sh_expense FOREIGN KEY (expense_id)
    REFERENCES expenses(id) ON DELETE CASCADE,

  CONSTRAINT fk_sh_user FOREIGN KEY (user_id)
    REFERENCES users(id) ON DELETE CASCADE,

  KEY idx_sh_user (user_id),

  CONSTRAINT chk_custom_amount
    CHECK (share_type = 'EQUAL' OR amount_assigned IS NOT NULL)
) ENGINE=InnoDB;

-- =========================
-- ATTACHMENTS (tickets/archivos)
-- =========================
CREATE TABLE attachments (
  id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
  expense_id BIGINT UNSIGNED NOT NULL,

  attachment_type ENUM('RECEIPT_IMAGE','OTHER') NOT NULL DEFAULT 'RECEIPT_IMAGE',
  file_path VARCHAR(700) NOT NULL,
  mime_type VARCHAR(80) NULL,
  file_size INT UNSIGNED NULL,

  ocr_text TEXT NULL,

  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  CONSTRAINT fk_att_expense FOREIGN KEY (expense_id)
    REFERENCES expenses(id) ON DELETE CASCADE,

  KEY idx_att_expense (expense_id)
) ENGINE=InnoDB;
