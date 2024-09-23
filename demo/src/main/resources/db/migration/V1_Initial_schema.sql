CREATE TYPE user_role AS ENUM ('ADMIN', 'MODERATOR', 'USER', 'CHECKER', 'SHIPPING', 'INVENTORY');

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role user_role NOT NULL
);

-- Insert default admin user
-- The password is 'password', hashed with BCrypt
INSERT INTO users (username, password, role)
VALUES ('admin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', 'ADMIN')
ON CONFLICT (username) DO NOTHING;