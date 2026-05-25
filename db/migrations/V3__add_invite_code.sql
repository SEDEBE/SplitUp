-- V3: Código de invitación único por grupo.
-- Permite a cualquier usuario unirse a un grupo sin que un admin le busque por email.

ALTER TABLE expense_groups
    ADD COLUMN invite_code VARCHAR(36) NULL UNIQUE
    COMMENT 'UUID generado al crear el grupo; cualquier usuario puede unirse con él';

-- Poblar grupos existentes que no tienen código todavía.
UPDATE expense_groups SET invite_code = UUID() WHERE invite_code IS NULL;
