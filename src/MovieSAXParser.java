import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.sql.DataSource;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MovieSAXParser extends DefaultHandler {

    private Map<String, String> existingMoviesCache;
    private Map<String, String> existingGenresCache;
    private Map<String, String> existingStarsCache;

    private String tempVal;
    private DataSource dataSource;
    private ExecutorService executorService;
    private Movie currentMovie;
    private boolean isGenre = false;

    public MovieSAXParser(DataSource dataSource, Map<String, String> moviesCache, Map<String, String> genresCache, Map<String, String> starsCache) {
        this.dataSource = dataSource;
        this.existingMoviesCache = moviesCache;
        this.existingGenresCache = genresCache;
        this.existingStarsCache = starsCache;
        this.executorService = Executors.newFixedThreadPool(8);
    }

    public void parseDocument(String filePath) {
        System.out.println("Parsing document: " + filePath);
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            sp.parse(new File(filePath), this);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, java.util.concurrent.TimeUnit.SECONDS)) {
                    System.err.println("Executor did not terminate in the specified time.");
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                System.err.println("Interrupted while awaiting termination.");
                executorService.shutdownNow();
            }
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tempVal = "";

        if (qName.equalsIgnoreCase("film")) {
            currentMovie = new Movie();
            currentMovie.setId(generateUniqueId()); // Generate a unique ID for the movie
        } else if (qName.equalsIgnoreCase("cat")) {
            isGenre = true;
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        tempVal += new String(ch, start, length);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("film")) {
            if (currentMovie.isValid()) {
                executorService.submit(() -> insertMovieIntoDB(currentMovie));
            } else {
                System.err.println("Skipping movie due to missing or invalid data: " + currentMovie);
            }
        } else if (qName.equalsIgnoreCase("t")) {
            if (!tempVal.trim().isEmpty()) {
                currentMovie.setTitle(tempVal.trim());
            } else {
                System.err.println("Missing title for movie. Skipping entry.");
            }
        } else if (qName.equalsIgnoreCase("year")) {
            try {
                currentMovie.setYear(Integer.parseInt(tempVal.trim()));
            } catch (NumberFormatException e) {
                System.err.println("Invalid year format for movie: " + currentMovie.getTitle() + ", setting year to NULL");
                currentMovie.setYear(0);
            }
        } else if (qName.equalsIgnoreCase("dirn")) {
            if (!tempVal.trim().isEmpty()) {
                currentMovie.setDirector(tempVal.trim());
            } else {
                System.err.println("Missing director for movie: " + currentMovie.getTitle() + ". Using NULL for director.");
            }
        } else if (qName.equalsIgnoreCase("cat") && isGenre) {
            String genre = tempVal.trim();  // Trim whitespace

            // Only proceed if genre is non-empty and not already cached
            if (!genre.isEmpty() && !existingGenresCache.containsKey(genre)) {
                executorService.submit(() -> insertGenreIntoDB(genre));
            }
            isGenre = false;
        }
    }

    private void insertMovieIntoDB(Movie movie) {
        String movieKey = movie.getKey();
        if (existingMoviesCache.containsKey(movieKey)) return;

        try (Connection dbConnection = dataSource.getConnection();
             PreparedStatement ps = dbConnection.prepareStatement("INSERT INTO movies (id, title, year, director) VALUES (?, ?, ?, ?)")) {
            ps.setString(1, movie.getId());
            ps.setString(2, movie.getTitle());

            if (movie.getYear() != 0) {
                ps.setInt(3, movie.getYear());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }

            ps.setString(4, movie.getDirector());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Inserted movie: " + movie);
                existingMoviesCache.put(movieKey, "inserted");
            } else {
                System.err.println("Failed to insert movie: " + movie);
            }
        } catch (SQLException e) {
            if (e instanceof java.sql.SQLIntegrityConstraintViolationException) {
                System.err.println("Integrity constraint violation while inserting movie: " + movie + ". Possible duplicate entry or null field.");
            } else {
                System.err.println("Error inserting movie: " + movie);
            }
        }
    }

    private void insertGenreIntoDB(String genre) {
        // Check if genre already exists in the database
        if (existingGenresCache.containsKey(genre)) {
            System.out.println("Genre already exists: " + genre);
            return;
        }

        // Only proceed if the genre does not exist in the cache
        try (Connection dbConnection = dataSource.getConnection();
             PreparedStatement checkStmt = dbConnection.prepareStatement("SELECT COUNT(*) FROM genres WHERE name = ?")) {
            checkStmt.setString(1, genre);

            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("Genre already in database: " + genre);
                    existingGenresCache.put(genre, "exists"); // Cache the genre to avoid repeated checks
                    return;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking genre existence: " + genre);
            e.printStackTrace();
        }

        // Insert the genre if it does not exist
        try (Connection dbConnection = dataSource.getConnection();
             PreparedStatement ps = dbConnection.prepareStatement("INSERT INTO genres (name) VALUES (?)")) {
            ps.setString(1, genre);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Inserted genre: " + genre);
                existingGenresCache.put(genre, "inserted");
            } else {
                System.err.println("Failed to insert genre: " + genre);
            }
        } catch (SQLException e) {
            System.err.println("Error inserting genre: " + genre);
            e.printStackTrace();
        }
    }

    private String generateUniqueId() {
        String id;
        boolean isUnique;
        do {
            id = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
            isUnique = checkIdUniqueness(id);
        } while (!isUnique);
        return id;
    }

    private boolean checkIdUniqueness(String id) {
        try (Connection dbConnection = dataSource.getConnection();
             PreparedStatement ps = dbConnection.prepareStatement("SELECT COUNT(*) FROM movies WHERE id = ?")) {
            ps.setString(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private class Movie {
        private String id;
        private String title;
        private int year;
        private String director;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public int getYear() { return year; }
        public void setYear(int year) { this.year = year; }

        public String getDirector() { return director; }
        public void setDirector(String director) { this.director = director; }

        public boolean isValid() {
            return title != null && !title.isEmpty() && year != 0 && director != null && !director.isEmpty();
        }

        public String getKey() { return title + "_" + year + "_" + director; }

        @Override
        public String toString() {
            return title + " (" + year + ") by " + director;
        }
    }
}
