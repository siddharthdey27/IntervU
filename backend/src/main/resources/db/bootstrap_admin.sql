-- Run manually against the production database after registering the admin account.
-- Replace the email below; do not store a plaintext password in this file.
UPDATE users
SET role = 'ADMIN'
WHERE email = 'replace-with-admin-email@example.com';

-- Confirm exactly one account was promoted.
SELECT id, email, role
FROM users
WHERE email = 'replace-with-admin-email@example.com';
