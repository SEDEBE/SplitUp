-- V2: Tabla de pagos confirmados entre miembros de un grupo.
-- Un SettlementRecord persiste el acto real de que from_user_id pagó
-- amount a to_user_id, a diferencia de las transferencias SUGERIDAS
-- que calcula el algoritmo greedy y nunca se almacenan en BD.

CREATE TABLE IF NOT EXISTS `settlements` (
    `id`           BIGINT UNSIGNED  NOT NULL AUTO_INCREMENT,
    `group_id`     BIGINT UNSIGNED  NOT NULL,
    `from_user_id` BIGINT UNSIGNED  NOT NULL,
    `to_user_id`   BIGINT UNSIGNED  NOT NULL,
    `amount`       DECIMAL(12,2)    NOT NULL,
    `settled_at`   DATETIME         NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `created_by`   BIGINT UNSIGNED  NOT NULL,

    PRIMARY KEY (`id`),

    CONSTRAINT `fk_settlements_group`
        FOREIGN KEY (`group_id`)    REFERENCES `expense_groups`(`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_settlements_from`
        FOREIGN KEY (`from_user_id`) REFERENCES `users`(`id`),
    CONSTRAINT `fk_settlements_to`
        FOREIGN KEY (`to_user_id`)   REFERENCES `users`(`id`),
    CONSTRAINT `fk_settlements_created_by`
        FOREIGN KEY (`created_by`)   REFERENCES `users`(`id`),

    CONSTRAINT `chk_settlements_amount`
        CHECK (`amount` > 0),
    CONSTRAINT `chk_settlements_different_users`
        CHECK (`from_user_id` <> `to_user_id`),

    INDEX `idx_settlements_group`      (`group_id`),
    INDEX `idx_settlements_settled_at` (`settled_at`)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
