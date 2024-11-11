import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MovieCastParserMain {

    private static final String DB_URL = System.getenv("DB_URL") != null
            ? System.getenv("DB_URL")
            : "jdbc:mysql://localhost:3306/moviedb"; // fallback to localhost

    private static final String USER = "mytestuser";
    private static final String PASS = "My6$Password";

    public static void main(String[] args) {
        // Initialize caches to track existing records in memory
        Map<String, String> actorsCache = new ConcurrentHashMap<>();
        Map<String, String> moviesCache = new ConcurrentHashMap<>();

        // Establish a connection to the database
        try (Connection dbConnection = DriverManager.getConnection(DB_URL, USER, PASS)) {
            System.out.println("Database connection established.");

            // Populate caches with existing database data
            populateCache(dbConnection, actorsCache, "SELECT name, birthYear FROM stars", "name", "birthYear");
            populateCache(dbConnection, moviesCache, "SELECT id FROM movies", "id");

            // Initialize and run the MovieCastParser
            MovieCastParser parser = new MovieCastParser(dbConnection, actorsCache, moviesCache);
            parser.parseDocument("/Users/wangemily/Desktop/cs122b/stanford-movies/casts124.xml");

        } catch (SQLException e) {
            System.err.println("Database connection failed.");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error running MovieCastParser.");
            e.printStackTrace();
        }
    }

    private static void populateCache(Connection connection, Map<String, String> cache, String query, String... columns) throws SQLException {
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
