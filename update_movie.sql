ALTER TABLE movies
    ADD price DECIMAL(10, 2) IF NOT EXISTS;

UPDATE movies
SET price = ROUND(5 + (RAND() * (20 - 5)), 2);

CREATE TABLE shopping_cart (
   cart_id INT AUTO_INCREMENT PRIMARY KEY,
   customer_id INT,
   movie_id VARCHAR(10),
   quantity INT,
   FOREIGN KEY (customer_id) REFERENCES customers(id),
   FOREIGN KEY (movie_id) REFERENCES movies(id)
);
