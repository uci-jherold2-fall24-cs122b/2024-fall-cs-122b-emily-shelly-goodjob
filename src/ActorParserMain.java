import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.naming.InitialContext;

public class ActorParserMain {
    public static void main(String[] args) {
        DataSource dataSource;

        try {
            InitialContext context = new InitialContext();
            dataSource = (DataSource) context.lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            System.err.println("Failed to initialize DataSource.");
            e.printStackTrace();
            return;
        }

        Map<String, String> actorsCache = new ConcurrentHashMap<>();
        Map<String, String> moviesCache = new ConcurrentHashMap<>();

        try (Connection dbConnection = dataSource.getConnection()) {
            System.out.println("Database connection established.");

            ActorParser parser = new ActorParser(dataSource, actorsCache, moviesCache);
//            parser.parseDocument("/Users/wangemily/Desktop/cs122b/stanford-movies/actors63.xml");

            parser.parseDocument("../stanford-movies/actors63.xml");

            // Ensure all tasks complete before closing the connection
            parser.awaitTermination();

        } catch (SQLException e) {
            System.err.println("Database connection failed.");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error running ActorParser.");
            e.printStackTrace();
        }
    }

    private static void populateCache(Connection connection, Map<String, String> cache, String query, String... columns) throws SQLException {
        try (var stmt = connection.createStatement(); var rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                StringBuilder key = new StringBuilder();
                for (String column : columns) {
                    key.append(rs.getString(column)).append("_");
                }
                cache.put(key.toString(), "exists");
            }
        }
        System.out.println("Cache populated with size: " + cache.size());
    }
}
