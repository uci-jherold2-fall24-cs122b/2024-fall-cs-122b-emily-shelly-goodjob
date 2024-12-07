DELIMITER //

-- Create a unique movie ID
CREATE PROCEDURE generate_unique_movie_id(OUT new_movie_id VARCHAR(10))
BEGIN
    movie_id_loop: REPEAT
        SET new_movie_id = CONCAT('tt', LPAD(FLOOR(RAND() * 10000000), 7, '0'));
    UNTIL NOT EXISTS (SELECT 1 FROM movies WHERE id = new_movie_id)
    END REPEAT movie_id_loop;
END //

DELIMITER ;

DELIMITER //

-- Insert a new movie if it doesn’t already exist
CREATE PROCEDURE add_movie_entry(
    IN p_title VARCHAR(100),
    IN p_year INT,
    IN p_director VARCHAR(100),
    OUT new_movie_id VARCHAR(10),
    OUT movie_exists BOOLEAN
)
BEGIN
    -- Check if the movie already exists to avoid duplicates
    IF EXISTS (SELECT 1 FROM movies WHERE title = p_title AND year = p_year AND director = p_director) THEN
        SET movie_exists = TRUE;
ELSE
        SET movie_exists = FALSE;
CALL generate_unique_movie_id(new_movie_id);
INSERT INTO movies (id, title, year, director) VALUES (new_movie_id, p_title, p_year, p_director);
END IF;
END //

DELIMITER ;


DELIMITER //

-- Insert a new star each time, even if the name and birth year are the same as an existing record
CREATE PROCEDURE add_or_get_star(
    IN p_star_name VARCHAR(100),
    IN p_birth_year INT,
    OUT new_star_id VARCHAR(10)
)
BEGIN
    -- Generate a unique star ID regardless of existing records
    SET new_star_id = CONCAT('nm', LPAD((SELECT COALESCE(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)), 0) + 1 FROM stars), 7, '0'));

    -- Insert the new star record
INSERT INTO stars (id, name, birthYear) VALUES (new_star_id, p_star_name, p_birth_year);
END //

DELIMITER ;


DELIMITER //

-- Insert a genre if it doesn’t exist, otherwise retrieve the existing genre_id
CREATE PROCEDURE add_or_get_genre(
    IN p_genre_name VARCHAR(32),
    OUT new_genre_id INT
)
BEGIN
    SET new_genre_id = (SELECT id FROM genres WHERE name = p_genre_name);
    IF new_genre_id IS NULL THEN
        INSERT INTO genres (name) VALUES (p_genre_name);
        SET new_genre_id = LAST_INSERT_ID();
    END IF;
END //
DELIMITER ;

DELIMITER //

CREATE PROCEDURE add_movie(
    IN p_title VARCHAR(100),
    IN p_year INT,
    IN p_director VARCHAR(100),
    IN p_star_name VARCHAR(100),
    IN p_birth_year INT,
    IN p_genre_name VARCHAR(32)
)
BEGIN
    DECLARE new_movie_id VARCHAR(10);
    DECLARE new_star_id VARCHAR(10);
    DECLARE new_genre_id INT;
    DECLARE movie_exists BOOLEAN;

    -- Attempt to add movie entry
CALL add_movie_entry(p_title, p_year, p_director, new_movie_id, movie_exists);

-- If the movie already exists, select duplicate status and exit
IF movie_exists THEN
SELECT 'Movie already exists, linking existing entries' AS message, 'duplicate' AS status;
ELSE
        -- Add or get star and genre, and link them
        CALL add_or_get_star(p_star_name, p_birth_year, new_star_id);
        INSERT IGNORE INTO stars_in_movies (starId, movieId) VALUES (new_star_id, new_movie_id);

CALL add_or_get_genre(p_genre_name, new_genre_id);
INSERT IGNORE INTO genres_in_movies (genreId, movieId) VALUES (new_genre_id, new_movie_id);

        -- Output the IDs for movie, star, and genre
SELECT new_movie_id AS movie_id, new_star_id AS star_id, new_genre_id AS genre_id, 'Movie added successfully' AS message, 'success' AS status;
END IF;
END //

DELIMITER ;
