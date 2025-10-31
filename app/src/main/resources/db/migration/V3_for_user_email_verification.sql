ALTER TABLE users
  ADD COLUMN email_verified boolean NOT NULL DEFAULT false;


--ALTER TABLE users ALTER COLUMN email_verified DROP DEFAULT;