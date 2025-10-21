INSERT INTO users (id, email, password_hash, roles, phone_country_code, phone_number, user_name, is_2fa_enabled)
SELECT
  gen_random_uuid(),
  'dev@example.com',
  '$2a$12$REPLACE_ME_WITH_REAL_BCRYPT_HASH',
  'USER',
  '+372',
  '555000123',
  'dev',
  false
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'dev@example.com');