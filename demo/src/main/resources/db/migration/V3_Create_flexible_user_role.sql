-- Create the roles table
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- Create the user_roles junction table
CREATE TABLE user_roles (
    user_id BIGINT REFERENCES users(id),
    role_id INT REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

-- Insert the initial roles
INSERT INTO roles (name) VALUES
('ADMIN'), ('MODERATOR'), ('USER'), ('CHECKER'), ('SHIPPING'), ('INVENTORY');

-- Migrate existing user roles
INSERT INTO user_roles (user_id, role_id)
SELECT id, (SELECT id FROM roles WHERE name = users.role::text)
FROM users;

-- Remove the old role column
ALTER TABLE users DROP COLUMN role;

-- Drop the user_role enum type
DROP TYPE user_role;