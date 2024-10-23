import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;


@WebServlet(name = "MainServlet", urlPatterns = "/search")
public class MainServlet extends HttpServlet {

    private DataSource dataSource;

    @Override
    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String genre = request.getParameter("genre");
        String titleInitial = request.getParameter("titleInitial");


        response.setContentType("application/json"); // Return JSON data
        PrintWriter out = response.getWriter();
        JSONArray jsonArray = new JSONArray();

        try {
            // Create a new connection to the database
            Connection dbCon = dataSource.getConnection();
            PreparedStatement statement;

            // Determine query type (search by genre, title initial, or general search)
            if (genre != null && !genre.trim().isEmpty()) {
                // Search by Genre
                String query = "SELECT movies.id, movies.title, movies.year, movies.director, " +
                        "GROUP_CONCAT(DISTINCT stars.name SEPARATOR ', ') AS stars " +
                        "FROM movies " +
                        "LEFT JOIN stars_in_movies ON movies.id = stars_in_movies.movieId " +
                        "LEFT JOIN stars ON stars.id = stars_in_movies.starId " +
                        "LEFT JOIN genres_in_movies ON movies.id = genres_in_movies.movieId " +
                        "LEFT JOIN genres ON genres.id = genres_in_movies.genreId " +
                        "WHERE genres.name = ? " +
                        "GROUP BY movies.id, movies.title, movies.year, movies.director";

                statement = dbCon.prepareStatement(query);
                statement.setString(1, genre);
            } else if (titleInitial != null && !titleInitial.trim().isEmpty()) {
                // Search by Title Initial
                String query = "SELECT movies.id, movies.title, movies.year, movies.director, " +
                        "GROUP_CONCAT(DISTINCT stars.name SEPARATOR ', ') AS stars " +
                        "FROM movies " +
                        "LEFT JOIN stars_in_movies ON movies.id = stars_in_movies.movieId " +
                        "LEFT JOIN stars ON stars.id = stars_in_movies.starId " +
                        "WHERE 1=1 ";

                // Add condition for alphanumeric or special characters
                if (titleInitial.equals("*")) {
                    // Match titles starting with non-alphanumeric characters
                    query += " AND movies.title REGEXP '^[^a-zA-Z0-9]'";
                } else {
                    // Match titles starting with a specific character
                    query += " AND movies.title LIKE ?";
                    titleInitial = titleInitial.toUpperCase() + "%"; // Case insensitive match
                }

                query += " GROUP BY movies.id, movies.title, movies.year, movies.director";
                statement = dbCon.prepareStatement(query);

                if (!titleInitial.equals("*")) {
                    statement.setString(1, titleInitial);
                }
            } else {
                // Retrieve search parameters
                String title = request.getParameter("title");
                String year = request.getParameter("year");
                String director = request.getParameter("director");
                String star = request.getParameter("star");

                // Create the SQL query dynamically
                String query = "SELECT movies.id, movies.title, movies.year, movies.director, " +
                        "GROUP_CONCAT(DISTINCT stars.name SEPARATOR ', ') AS stars " +
                        "FROM movies " +
                        "LEFT JOIN stars_in_movies ON movies.id = stars_in_movies.movieId " +
                        "LEFT JOIN stars ON stars.id = stars_in_movies.starId " +
                        "WHERE 1=1";

                if (title != null && !title.trim().isEmpty()) {
                    query += " AND movies.title LIKE ?";
                    title = "%" + title + "%";
                }
                if (year != null && !year.trim().isEmpty()) {
                    query += " AND movies.year = ?";
                }
                if (director != null && !director.trim().isEmpty()) {
                    query += " AND movies.director LIKE ?";
                    director = "%" + director + "%";
                }
                if (star != null && !star.trim().isEmpty()) {
                    query += " AND stars.name LIKE ?";
                    star = "%" + star + "%";
                }

                query += " GROUP BY movies.id, movies.title, movies.year, movies.director";

                statement = dbCon.prepareStatement(query);

                int index = 1;
                if (title != null && !title.trim().isEmpty()) statement.setString(index++, title);
                if (year != null && !year.trim().isEmpty()) statement.setInt(index++, Integer.parseInt(year));
                if (director != null && !director.trim().isEmpty()) statement.setString(index++, director);
                if (star != null && !star.trim().isEmpty()) statement.setString(index++, star);
            }

            ResultSet rs = statement.executeQuery();

            // Convert result set to JSON
            while (rs.next()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("movie_id", rs.getString("id"));
                jsonObject.put("movie_title", rs.getString("title"));
                jsonObject.put("movie_year", rs.getString("year"));
                jsonObject.put("movie_director", rs.getString("director"));
                jsonObject.put("movie_stars", rs.getString("stars") == null ? "" : rs.getString("stars"));
                jsonArray.put(jsonObject);
            }

            // Output JSON
            out.write(jsonArray.toString());

            rs.close();
            statement.close();
            dbCon.close();

        } catch (SQLException e) {
            request.getServletContext().log("SQL Error: ", e);
            out.write("{\"error\": \"SQL error in doGet: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            request.getServletContext().log("General Error: ", e);
            out.write("{\"error\": \"Error in doGet: " + e.getMessage() + "\"}");
        }
        out.close();
    }
}

