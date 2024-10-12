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
                    "GROUP_CONCAT(DISTINCT g.name ORDER BY g.name) AS genres, " +
                    "GROUP_CONCAT(DISTINCT s.name ORDER BY s.name) AS stars, " +
                    "GROUP_CONCAT(DISTINCT s.id ORDER BY s.name) AS starIds " +
                    "FROM movies m " +
                    "JOIN ratings r ON r.movieId = m.id " +
                    "LEFT JOIN genres_in_movies gim ON gim.movieId = m.id " +
                    "LEFT JOIN genres g ON g.id = gim.genreId " +
                    "LEFT JOIN stars_in_movies sim ON sim.movieId = m.id " +
                    "LEFT JOIN stars s ON s.id = sim.starId " +
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
                String movieTitle = rs.getString("movieTitle");
                String movieYear = rs.getString("movieYear");
                String movieDirector = rs.getString("movieDirector");
                String movieRating = rs.getString("movieRating");
                String movieGenres = rs.getString("genres");
                String movieStars = rs.getString("stars");
                String starIds = rs.getString("starIds");

                // Add movie
                jsonObject.addProperty("movie_title", movieTitle);
                jsonObject.addProperty("movie_year", movieYear);
                jsonObject.addProperty("movie_director", movieDirector);
                jsonObject.addProperty("movie_rating", movieRating);
                jsonObject.addProperty("movie_genres", movieGenres);

                // Add stars
                JsonArray starsArray = new JsonArray();
                String[] starNames = movieStars.split(",");
                String[] star_Ids = starIds.split(",");
                for (int i = 0; i < starNames.length; i++) {
                    JsonObject star = new JsonObject();
                    star.addProperty("star_id", star_Ids[i]);
                    star.addProperty("star_name", starNames[i]);
                    starsArray.add(star);
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
