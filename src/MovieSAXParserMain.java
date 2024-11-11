import java.sql.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MovieSAXParserMain {

    private static final String DB_URL = System.getenv("DB_URL") != null
            ? System.getenv("DB_URL")
            : "jdbc:mysql://localhost:3306/moviedb"; // fallback to localhost

    private static final String USER = "mytestuser";
    private static final String PASS = "My6$Password";

    public static void main(String[] args) {
        Map<String, String> moviesCache = new ConcurrentHashMap<>();
        Map<String, String> genresCache = new ConcurrentHashMap<>();
        Map<String, String> starsCache = new ConcurrentHashMap<>();

        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASS)) {
            populateCache(connection, moviesCache, "SELECT title, year, director FROM movies", "title", "year", "director");
            populateCache(connection, genresCache, "SELECT name FROM genres", "name");
            populateCache(connection, starsCache, "SELECT name, birthYear FROM stars", "name", "birthYear");

            MovieSAXParser parser = new MovieSAXParser(connection, moviesCache, genresCache, starsCache);
//            parser.parseDocument("/Users/wangemily/Desktop/cs122b/stanford-movies/mains243.xml");
            parser.parseDocument("../stanford-movies/mains243.xml");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void populateCache(Connection connection, Map<String, String> cache, String query, String... columns) throws Exception {
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                StringBuilder key = new StringBuilder();
                for (String column : columns) {
                    key.append(rs.getString(column)).append("_");
                }
                cache.put(key.toString(), "exists");
            }
        } catch (SQLException e) {
            System.err.println("Error populating cache: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("Cache populated with size: " + cache.size());
    }

}
