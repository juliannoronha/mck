-- Create pac table
CREATE TABLE pac (
    id BIGSERIAL PRIMARY KEY,
    user_answer_id BIGINT NOT NULL,
    store VARCHAR(255) NOT NULL,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    pouches_checked INTEGER,
    CONSTRAINT fk_user_answer FOREIGN KEY (user_answer_id) REFERENCES user_answer(id)
);

-- Transfer data from user_answer to pac
INSERT INTO pac (user_answer_id, store, start_time, end_time, pouches_checked)
SELECT id, store, start_time, end_time, pouches_checked
FROM user_answer;

-- Modify pac table
ALTER TABLE pac
ADD COLUMN store VARCHAR(255) NOT NULL,
ADD COLUMN start_time TIMESTAMP,
ADD COLUMN end_time TIMESTAMP,
ADD COLUMN pouches_checked INTEGER;

-- Modify user_answer table
ALTER TABLE user_answer
DROP COLUMN store,
DROP COLUMN start_time,
DROP COLUMN end_time,
DROP COLUMN pouches_checked;

-- Add submission_date column to user_answer if it doesn't exist
ALTER TABLE user_answer
ADD COLUMN IF NOT EXISTS submission_date DATE;

-- Update submission_date with the date part of start_time
UPDATE user_answer ua
SET submission_date = DATE(p.start_time)
FROM pac p
WHERE ua.id = p.user_answer_id;

-- Update pac table to use TIME instead of TIMESTAMP
ALTER TABLE pac
ALTER COLUMN start_time TYPE TIME,
ALTER COLUMN end_time TYPE TIME;