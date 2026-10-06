-- Repairs the BCrypt hash for the users defined in seed_test_data.sql.
-- Test password after executing this script: password

UPDATE users
SET password = '$2a$12$lRjI71wrqakSjaVFLQZSZ.xmq2.wn6LbBGQgJWSGndpobIG0ygwl6'
WHERE email IN (
    'sofia@onegym.test',
    'lucas@onegym.test',
    'valentina@onegym.test',
    'admin@onegym.test'
);
