insert into users(id, email, password_hash, roles)
values (gen_random_uuid(), 'dev@example.com', '{bcrypt}$2a$10$dummyhash', 'USER');