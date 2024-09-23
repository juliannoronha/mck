-- Insert users
INSERT INTO users (username, password, role) 
VALUES ('admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'ADMIN') 
ON CONFLICT (username) DO NOTHING;
