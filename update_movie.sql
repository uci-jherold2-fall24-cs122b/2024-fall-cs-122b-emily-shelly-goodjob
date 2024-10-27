-- Check if the 'price' column exists before adding it
-- This creates a variable to check if the column exists
SET @columnExists = (SELECT COUNT(*)
                     FROM INFORMATION_SCHEMA.COLUMNS
                     WHERE TABLE_NAME = 'movies'
                     AND COLUMN_NAME = 'price');

-- Add 'price' column only if it does not exist
IF @columnExists = 0 THEN
ALTER TABLE movies
    ADD COLUMN price DECIMAL(10, 2);
END IF;

-- Update 'price' column with random values between 5 and 20
UPDATE movies
SET price = ROUND(5 + (RAND() * (20 - 5)), 2);

-- Drop 'shopping_cart' table if it already exists
DROP TABLE IF EXISTS shopping_cart;

-- Create 'shopping_cart' table
CREATE TABLE shopping_cart (
                               cart_id INT AUTO_INCREMENT PRIMARY KEY,
                               customer_id INT,
                               movie_id VARCHAR(10),
                               quantity INT,
                               FOREIGN KEY (customer_id) REFERENCES customers(id),
                               FOREIGN KEY (movie_id) REFERENCES movies(id)
);
