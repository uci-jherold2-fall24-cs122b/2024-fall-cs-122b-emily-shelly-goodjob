import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

// Declaring a WebServlet called SingleStarServlet, which maps to url "/api/single-star"
@WebServlet(name = "SingleMovieServlet", urlPatterns = "/api/single-movie")
public class SingleMovieServlet extends HttpServlet {
    private static final long serialVersionUID = 2L;

    // Create a dataSource which registered in web.xml
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     * response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Retrieve parameter id from url request.
        String id = request.getParameter("id");

        // The log message can be found in localhost log
        request.getServletContext().log("getting id: " + id);

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {
            // Get a connection from dataSource
            String query = "SELECT m.title AS movieTitle, " +
                    "m.year AS movieYear, " +
                    "m.director AS movieDirector, " +
                    "r.rating AS movieRating, " +
                    "(SELECT GROUP_CONCAT(g.name ORDER BY g.name SEPARATOR ', ') " +
                    " FROM genres g " +
                    " JOIN genres_in_movies gm ON gm.genreId = g.id " +
                    " WHERE gm.movieId = m.id) AS genres, " +
                    "(SELECT GROUP_CONCAT(star_info.star_name SEPARATOR ', ') " +
                    " FROM ( " +
                    "   SELECT s.name AS star_name " +
                    "   FROM stars s " +
                    "   JOIN stars_in_movies sim ON sim.starId = s.id " +
                    "   WHERE sim.movieId = m.id " +
                    "   ORDER BY (SELECT COUNT(*) FROM stars_in_movies WHERE starId = s.id) DESC, s.name " +
                    ") AS star_info) AS stars, " +
                    "(SELECT GROUP_CONCAT(star_info.star_id SEPARATOR ', ') " +
                    " FROM ( " +
                    "   SELECT s.id AS star_id " +
                    "   FROM stars s " +
                    "   JOIN stars_in_movies sim ON sim.starId = s.id " +
                    "   WHERE sim.movieId = m.id " +
                    "   ORDER BY (SELECT COUNT(*) FROM stars_in_movies WHERE starId = s.id) DESC, s.name " +
                    ") AS star_info) AS starIds " +
                    "FROM movies m " +
                    "LEFT JOIN ratings r ON r.movieId = m.id " +
                    "WHERE m.id = ? " +
                    "GROUP BY m.id";

            // Declare our statement
            PreparedStatement statement = conn.prepareStatement(query);

            // Set the parameter represented by "?" in the query to the id we get from url,
            // num 1 indicates the first "?" in the query
            statement.setString(1, id);

            // Perform the query
            ResultSet rs = statement.executeQuery();

            JsonObject jsonObject = new JsonObject();

            // Iterate through each row of rs
            if (rs.next()) {
                jsonObject.addProperty("movie_title", rs.getString("movieTitle"));
                jsonObject.addProperty("movie_year", rs.getString("movieYear"));
                jsonObject.addProperty("movie_director", rs.getString("movieDirector"));
                jsonObject.addProperty("movie_rating", rs.getString("movieRating"));

                // Add stars
                JsonArray starsArray = new JsonArray();
                String[] starNames = rs.getString("stars").split(", ");
                String[] starIds = rs.getString("starIds").split(", ");
                for (int i = 0; i < starNames.length; i++) {
                    JsonObject star = new JsonObject();
                    star.addProperty("star_id", starIds[i]);
                    star.addProperty("star_name", starNames[i]);
                    starsArray.add(star);
                }

                // Convert genres to hyperlinked format
                String genres = rs.getString("genres");
                if (genres != null) {
                    String[] genreList = genres.split(", ");
                    StringBuilder genreLinks = new StringBuilder();
                    for (String genre : genreList) {
                        genreLinks.append("<a href='result.html?genre=").append(genre).append("' class='page-subtitle-text-hyperlink'>")
                                .append(genre).append("</a>, ");
                    }
                    // Remove the trailing comma and space
                    if (genreLinks.length() > 2) {
                        genreLinks.setLength(genreLinks.length() - 2);
                    }
                    jsonObject.addProperty("movie_genres", genreLinks.toString());
                } else {
                    jsonObject.addProperty("movie_genres", "");
                }

                jsonObject.add("movie_stars", starsArray);
            }

            rs.close();
            statement.close();

            // Write JSON string to output
            out.write(jsonObject.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {
            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Log error to localhost log
            request.getServletContext().log("Error:", e);
            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }

}
