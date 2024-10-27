ALTER TABLE movies
    ADD COLUMN price DECIMAL(10, 2);

-- Generate 'price' column with random values between 5 and 20
UPDATE movies
SET price = ROUND(5 + (RAND() * (20 - 5)), 2);

DESCRIBE movies;

SELECT m.title, sc.movie_id, sc.quantity, m.price
FROM shopping_cart sc
         JOIN movies m ON sc.movie_id = m.id
WHERE sc.customer_id = 490001;
