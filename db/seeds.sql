USE splitup;

INSERT INTO categories (name, icon) VALUES
('Supermercado', 'cart'),
('Restaurante', 'utensils'),
('Transporte', 'bus'),
('Ocio', 'gamepad');

-- Usuarios demo
INSERT INTO users (display_name, email, avatar_url) VALUES
('Alejandro', 'alejandro@example.com', NULL),
('Miguel', 'miguel@example.com', NULL),
('Laura', 'laura@example.com', NULL);

-- Grupo demo
INSERT INTO expense_groups (name, description, created_by) VALUES
('Viaje Lisboa', 'Gastos del viaje', 1);

-- Miembros
INSERT INTO group_members (group_id, user_id, role) VALUES
(1, 1, 'OWNER'),
(1, 2, 'MEMBER'),
(1, 3, 'MEMBER');

-- Gasto demo
INSERT INTO expenses (group_id, payer_user_id, category_id, title, total_amount, currency, expense_date, note, split_mode)
VALUES (1, 1, 1, 'Mercadona', 30.00, 'EUR', '2026-01-29', 'Compra para el piso', 'EQUAL');

-- Participantes del gasto
INSERT INTO expense_shares (expense_id, user_id, share_type, amount_assigned) VALUES
(1, 1, 'EQUAL', NULL),
(1, 2, 'EQUAL', NULL),
(1, 3, 'EQUAL', NULL);

-- Adjuntar ticket (ruta local de ejemplo)
INSERT INTO attachments (expense_id, attachment_type, file_path, mime_type, file_size, ocr_text)
VALUES (1, 'RECEIPT_IMAGE', 'storage/tickets/2026-01/mercadona_001.jpg', 'image/jpeg', 245000, NULL);
