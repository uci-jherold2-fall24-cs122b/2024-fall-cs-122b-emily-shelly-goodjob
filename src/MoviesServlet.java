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
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;


// Declaring a WebServlet called StarsServlet, which maps to url "/api/stars"
@WebServlet(name = "MoviesServlet", urlPatterns = "/api/movies")
public class MoviesServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Create a dataSource which registered in web.
    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType("application/json"); // Response mime type

        // Output stream to STDOUT
        PrintWriter out = response.getWriter();

        // Get a connection from dataSource and let resource manager close the connection after usage.
        try (Connection conn = dataSource.getConnection()) {

            // Declare our statement
            Statement statement = conn.createStatement();

            String query = "SELECT m.id, m.title, m.year, m.director, r.rating, " +
                    "(SELECT GROUP_CONCAT(g.name ORDER BY g.name) " +
                    " FROM genres g " +
                    " JOIN genres_in_movies gm ON gm.genreId = g.id " +
                    " WHERE gm.movieId = m.id " +
                    " ORDER BY g.name " +
                    " LIMIT 3) AS genres, " +
                    "(SELECT GROUP_CONCAT(s.name ORDER BY s.name) " +
                    " FROM stars s " +
                    " JOIN stars_in_movies sim ON sim.starId = s.id " +
                    " WHERE sim.movieId = m.id " +
                    " ORDER BY s.name " +
                    " LIMIT 3) AS stars, " +
                    "(SELECT GROUP_CONCAT(s.id ORDER BY s.name) " +
                    " FROM stars s " +
                    " JOIN stars_in_movies sim ON sim.starId = s.id " +
                    " WHERE sim.movieId = m.id " +
                    " ORDER BY s.name " +
                    " LIMIT 3) AS star_ids " +
                    "FROM movies m " +
                    "JOIN ratings r ON m.id = r.movieId " +
                    "GROUP BY m.id " +
                    "ORDER BY r.rating DESC " +
                    "LIMIT 20;";

            // Perform the query
            ResultSet rs = statement.executeQuery(query);

            JsonArray jsonArray = new JsonArray();

            // Iterate through each row of rs
            while (rs.next()) {
                String movie_id = rs.getString("id");
                String movie_title = rs.getString("title");
                String movie_year = rs.getString("year");
                String movie_director = rs.getString("director");
                String movie_rating = rs.getString("rating");
                String movie_genres = rs.getString("genres");
                String movie_stars = rs.getString("stars");
                String movie_star_ids = rs.getString("star_ids");

//                String star_id = rs.getString("id");
//                String star_name = rs.getString("name");
//                String star_dob = rs.getString("birthYear");

                // Create a JsonObject based on the data we retrieve from rs
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("movie_id", movie_id);
                jsonObject.addProperty("movie_title", movie_title);
                jsonObject.addProperty("movie_year", movie_year);
                jsonObject.addProperty("movie_director", movie_director);
                jsonObject.addProperty("movie_rating", movie_rating);
//                jsonObject.addProperty("movie_genres", movie_genres);

                // Genres
                String[] genresArray = movie_genres.split(",");
                String limitedGenres = String.join(", ", Arrays.copyOfRange(genresArray, 0, Math.min(3, genresArray.length)));
                jsonObject.addProperty("movie_genres", limitedGenres);

                // Stars
                JsonArray starsArray = new JsonArray();
                String[] starNames = movie_stars.split(",");
                String[] starIds = movie_star_ids.split(",");
                int starCount = Math.min(3, starNames.length);
                for (int i = 0; i < starCount; i ++) {
                    JsonObject star = new JsonObject();
                    star.addProperty("star_id", starIds[i]);
                    star.addProperty("star_name", starNames[i]);
                    starsArray.add(star);
                }
                jsonObject.add("movie_stars", starsArray);

//                jsonObject.addProperty("star_id", star_id);
//                jsonObject.addProperty("star_name", star_name);
//                jsonObject.addProperty("star_dob", star_dob);
                System.out.println(jsonObject.toString());  // Log to see if movie_director is there

                jsonArray.add(jsonObject);
            }
            rs.close();
            statement.close();

            // Log to localhost log
            request.getServletContext().log("getting " + jsonArray.size() + " results");

            // Write JSON string to output
            out.write(jsonArray.toString());
            // Set response status to 200 (OK)
            response.setStatus(200);

        } catch (Exception e) {

            // Write error message JSON object to output
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("errorMessage", e.getMessage());
            out.write(jsonObject.toString());

            // Set response status to 500 (Internal Server Error)
            response.setStatus(500);
        } finally {
            out.close();
        }

        // Always remember to close db connection after usage. Here it's done by try-with-resources

    }
}
