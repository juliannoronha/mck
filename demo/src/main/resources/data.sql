INSERT INTO roles (name) VALUES ('ROLE_ADMIN') ON DUPLICATE KEY UPDATE name = 'ROLE_ADMIN';
INSERT INTO roles (name) VALUES ('ROLE_USER') ON DUPLICATE KEY UPDATE name = 'ROLE_USER';

-- Insert admin user (password is 'admin' - make sure to change this in production!)
INSERT INTO users (username, password, role_id) 
SELECT 'admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', id 
FROM roles WHERE name = 'ROLE_ADMIN'
ON DUPLICATE KEY UPDATE username = 'admin';