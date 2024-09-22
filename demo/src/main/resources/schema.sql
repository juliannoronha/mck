CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL
);

ALTER TABLE users 
ALTER COLUMN role SET DATA TYPE VARCHAR(255);

-- Drop the existing constraint if it exists
ALTER TABLE users 
DROP CONSTRAINT IF EXISTS users_role_check;

-- Add a new check constraint
ALTER TABLE users 
ADD CONSTRAINT users_role_check 
CHECK (role IN ('ADMIN', 'MODERATOR', 'USER', 'CHECKER', 'SHIPPING', 'INVENTORY'));