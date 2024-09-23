CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(255) NOT NULL
);

ALTER TABLE users 
ADD CONSTRAINT users_role_check 
CHECK (role IN ('ADMIN', 'MODERATOR', 'USER', 'CHECKER', 'SHIPPING', 'INVENTORY'));

CREATE SEQUENCE IF NOT EXISTS user_id_seq START WITH 1 INCREMENT BY 1;

CREATE INDEX IF NOT EXISTS idx_user_answer_name ON user_answer(name);
CREATE INDEX IF NOT EXISTS idx_user_answer_submission_date ON user_answer(submission_date);