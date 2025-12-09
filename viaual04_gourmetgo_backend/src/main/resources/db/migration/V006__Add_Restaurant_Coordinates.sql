-- Add latitude and longitude columns to restaurant table
ALTER TABLE restaurant 
ADD COLUMN latitude DECIMAL(10, 8),
ADD COLUMN longitude DECIMAL(11, 8);

-- Update existing restaurants with sample coordinates around Budapest
UPDATE restaurant SET 
    latitude = 47.4979 + (RANDOM() - 0.5) * 0.02,
    longitude = 19.0402 + (RANDOM() - 0.5) * 0.02
WHERE latitude IS NULL;