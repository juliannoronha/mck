-- Temporarily change existing values to a string type
ALTER TABLE users ALTER COLUMN role TYPE VARCHAR(255);

-- Drop the existing enum type
DROP TYPE user_role;

-- Recreate the enum type with all values
CREATE TYPE user_role AS ENUM ('ADMIN', 'MODERATOR', 'USER', 'CHECKER', 'SHIPPING', 'INVENTORY');

-- Change the column back to the enum type
ALTER TABLE users ALTER COLUMN role TYPE user_role USING role::user_role;