
CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO users
(id, email, password_hash, roles, phone_country_code, phone, is_2fa_enabled)
VALUES
    (gen_random_uuid(), 'dev@example.com', '{bcrypt}$2a$10$dummyhash', 'USER', '+372', '555000123', false);