-- Add fullName column to restaurant table for consistency with user interface
ALTER TABLE restaurant ADD COLUMN full_name VARCHAR(255);

-- Populate fullName with existing restaurant names
UPDATE restaurant SET full_name = name WHERE full_name IS NULL;

-- Remove redundant columns
ALTER TABLE restaurant DROP COLUMN IF EXISTS owner_name;
ALTER TABLE restaurant DROP COLUMN IF EXISTS name;