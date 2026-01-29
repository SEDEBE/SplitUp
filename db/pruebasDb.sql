USE splitup;
SHOW TABLES;

SELECT * FROM users;
SELECT * FROM expense_groups;
SELECT * FROM group_members;
SELECT * FROM expenses;
SELECT * FROM expense_shares;
SELECT * FROM attachments;

-- Ver gastos con su grupo y pagador
SELECT e.id, e.title, e.total_amount, g.name AS grupo, u.display_name AS pagador
FROM expenses e
JOIN expense_groups g ON g.id = e.group_id
JOIN users u ON u.id = e.payer_user_id;

-- Ver participantes de un gasto
SELECT es.expense_id, u.display_name, es.share_type, es.amount_assigned
FROM expense_shares es
JOIN users u ON u.id = es.user_id
WHERE es.expense_id = 1;

-- Ver adjuntos del gasto
SELECT a.id, a.expense_id, a.file_path, a.mime_type, a.file_size
FROM attachments a
WHERE a.expense_id = 1;
