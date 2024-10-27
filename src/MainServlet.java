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
        int page = Integer.parseInt(request.getParameter("page"));
        int moviesPerPage = Integer.parseInt(request.getParameter("moviesPerPage"));
        String sortBy = request.getParameter("sortBy");

        response.setContentType("application/json"); // Return JSON data
        PrintWriter out = response.getWriter();
        JSONArray jsonArray = new JSONArray();

        String orderByClause = getOrderByClause(sortBy);
        String limitOffsetClause = " LIMIT ? OFFSET ? ";

        // Create a new connection to the database
        try (Connection dbCon = dataSource.getConnection()) {
            PreparedStatement statement;

            // Determine query type (search by genre, title initial, or general search)
            if (genre != null && !genre.trim().isEmpty()) {
                // Search by Genre
                String query = "SELECT m.id, m.title, m.year, m.director, r.rating, " +
                        "(SELECT GROUP_CONCAT(g.name ORDER BY g.name SEPARATOR ', ') " +
                        " FROM genres g " +
                        " JOIN genres_in_movies gm ON gm.genreId = g.id " +
                        " WHERE gm.movieId = m.id " +
                        " ORDER BY g.name LIMIT 3) AS genres, " +
                        "(SELECT GROUP_CONCAT(star_info.star_name SEPARATOR ', ') " +
                        " FROM ( " +
                        "   SELECT s.name AS star_name " +
                        "   FROM stars s " +
                        "   JOIN stars_in_movies sim ON sim.starId = s.id " +
                        "   WHERE sim.movieId = m.id " +
                        "   ORDER BY (SELECT COUNT(*) FROM stars_in_movies WHERE starId = s.id) DESC, s.name " +
                        "   LIMIT 3 " +
                        ") AS star_info) AS stars, " +
                        "(SELECT GROUP_CONCAT(star_info.star_id SEPARATOR ', ') " +
                        " FROM ( " +
                        "   SELECT s.id AS star_id " +
                        "   FROM stars s " +
                        "   JOIN stars_in_movies sim ON sim.starId = s.id " +
                        "   WHERE sim.movieId = m.id " +
                        "   ORDER BY (SELECT COUNT(*) FROM stars_in_movies WHERE starId = s.id) DESC, s.name " +
                        "   LIMIT 3 " +
                        ") AS star_info) AS star_ids " +
                        "FROM movies m " +
                        "LEFT JOIN ratings r ON m.id = r.movieId " +
                        "LEFT JOIN genres_in_movies gm ON gm.movieId = m.id " +
                        "LEFT JOIN genres g ON g.id = gm.genreId " +
                        "WHERE g.name = ? " +
                        "GROUP BY m.id, m.title, m.year, m.director, r.rating " +
                         orderByClause + limitOffsetClause;

                statement = dbCon.prepareStatement(query);
                statement.setString(1, genre);
                statement.setInt(2, moviesPerPage);
                statement.setInt(3, page * moviesPerPage);
            } else if (titleInitial != null && !titleInitial.trim().isEmpty()) {
                // Search by Title Initial
                String query = "SELECT m.id, m.title, m.year, m.director, r.rating, " +
                        "(SELECT GROUP_CONCAT(g.name ORDER BY g.name SEPARATOR ', ') " +
                        " FROM genres g " +
                        " JOIN genres_in_movies gm ON gm.genreId = g.id " +
                        " WHERE gm.movieId = m.id " +
                        " ORDER BY g.name LIMIT 3) AS genres, " +
                        "(SELECT GROUP_CONCAT(star_info.star_name SEPARATOR ', ') " +
                        " FROM ( " +
                        "   SELECT s.name AS star_name " +
                        "   FROM stars s " +
                        "   JOIN stars_in_movies sim ON sim.starId = s.id " +
                        "   WHERE sim.movieId = m.id " +
                        "   ORDER BY (SELECT COUNT(*) FROM stars_in_movies WHERE starId = s.id) DESC, s.name " +
                        "   LIMIT 3 " +
                        ") AS star_info) AS stars, " +
                        "(SELECT GROUP_CONCAT(star_info.star_id SEPARATOR ', ') " +
                        " FROM ( " +
                        "   SELECT s.id AS star_id " +
                        "   FROM stars s " +
                        "   JOIN stars_in_movies sim ON sim.starId = s.id " +
                        "   WHERE sim.movieId = m.id " +
                        "   ORDER BY (SELECT COUNT(*) FROM stars_in_movies WHERE starId = s.id) DESC, s.name " +
                        "   LIMIT 3 " +
                        ") AS star_info) AS star_ids " +
                        "FROM movies m " +
                        "LEFT JOIN ratings r ON m.id = r.movieId " +
                        "WHERE 1=1 ";

                // Add condition for alphanumeric or special characters
                if (titleInitial.equals("*")) {
                    // Match titles starting with non-alphanumeric characters
                    query += " AND m.title REGEXP '^[^a-zA-Z0-9]'";
                } else {
                    // Match titles starting with a specific character
                    query += " AND m.title LIKE ?";
                    titleInitial = titleInitial.toUpperCase() + "%"; // Case insensitive match
                }

                query += " GROUP BY m.id, m.title, m.year, m.director, r.rating " +
                        orderByClause + limitOffsetClause;

                statement = dbCon.prepareStatement(query);

                if (!titleInitial.equals("*")) {
                    statement.setString(1, titleInitial);
                    statement.setInt(2, moviesPerPage);
                    statement.setInt(3, page * moviesPerPage);
                } else {
                    statement.setInt(1, moviesPerPage);
                    statement.setInt(2, page * moviesPerPage);
                }
            } else {
                // Retrieve search parameters
                String title = request.getParameter("title");
                String year = request.getParameter("year");
                String director = request.getParameter("director");
                String star = request.getParameter("star");

                // Create the SQL query dynamically
                StringBuilder queryBuilder = new StringBuilder();
                queryBuilder.append("SELECT m.id, m.title, m.year, m.director, r.rating, ");
                queryBuilder.append("(SELECT GROUP_CONCAT(g.name ORDER BY g.name SEPARATOR ', ') ");
                queryBuilder.append(" FROM genres g ");
                queryBuilder.append(" JOIN genres_in_movies gm ON gm.genreId = g.id ");
                queryBuilder.append(" WHERE gm.movieId = m.id ");
                queryBuilder.append(" ORDER BY g.name LIMIT 3) AS genres, ");
                queryBuilder.append("(SELECT GROUP_CONCAT(star_info.star_name SEPARATOR ', ') ");
                queryBuilder.append(" FROM ( ");
                queryBuilder.append("   SELECT s.name AS star_name ");
                queryBuilder.append("   FROM stars s ");
                queryBuilder.append("   JOIN stars_in_movies sim ON sim.starId = s.id ");
                queryBuilder.append("   WHERE sim.movieId = m.id ");
                queryBuilder.append("   ORDER BY (SELECT COUNT(*) FROM stars_in_movies WHERE starId = s.id) DESC, s.name ");
                queryBuilder.append("   LIMIT 3 ");
                queryBuilder.append(") AS star_info) AS stars, ");
                queryBuilder.append("(SELECT GROUP_CONCAT(star_info.star_id SEPARATOR ', ') ");
                queryBuilder.append(" FROM ( ");
                queryBuilder.append("   SELECT s.id AS star_id ");
                queryBuilder.append("   FROM stars s ");
                queryBuilder.append("   JOIN stars_in_movies sim ON sim.starId = s.id ");
                queryBuilder.append("   WHERE sim.movieId = m.id ");
                queryBuilder.append("   ORDER BY (SELECT COUNT(*) FROM stars_in_movies WHERE starId = s.id) DESC, s.name ");
                queryBuilder.append("   LIMIT 3 ");
                queryBuilder.append(") AS star_info) AS star_ids ");
                queryBuilder.append("FROM movies m ");
                queryBuilder.append("LEFT JOIN ratings r ON m.id = r.movieId ");
                queryBuilder.append("WHERE 1=1");

                if (title != null && !title.trim().isEmpty()) {
                    queryBuilder.append(" AND m.title LIKE ?");
                }
                if (year != null && !year.trim().isEmpty()) {
                    queryBuilder.append(" AND m.year = ?");
                }
                if (director != null && !director.trim().isEmpty()) {
                    queryBuilder.append(" AND m.director LIKE ?");
                }
                if (star != null && !star.trim().isEmpty()) {
                    queryBuilder.append(" AND EXISTS (SELECT 1 FROM stars_in_movies sim JOIN stars s ON s.id = sim.starId WHERE sim.movieId = m.id AND s.name LIKE ?)");
                }

//                query += " GROUP BY movies.id, movies.title, movies.year, movies.director";
                queryBuilder.append(" GROUP BY m.id, m.title, m.year, m.director, r.rating")
                            .append(orderByClause).append(limitOffsetClause);

                String query = queryBuilder.toString();
                statement = dbCon.prepareStatement(query);

                int index = 1;
                if (title != null && !title.trim().isEmpty()) statement.setString(index++, "%" + title + "%");
                if (year != null && !year.trim().isEmpty()) statement.setInt(index++, Integer.parseInt(year));
                if (director != null && !director.trim().isEmpty()) statement.setString(index++, "%" + director + "%");
                if (star != null && !star.trim().isEmpty()) statement.setString(index++, "%" + star + "%");
                statement.setInt(index++, moviesPerPage);
                statement.setInt(index, page * moviesPerPage);
            }

            ResultSet rs = statement.executeQuery();

            // Convert result set to JSON
            while (rs.next()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("movie_id", rs.getString("id"));
                jsonObject.put("movie_title", "<a href='single-movie.html?id=" + rs.getString("id") + "'>" + rs.getString("title") + "</a>");
                jsonObject.put("movie_year", rs.getString("year"));
                jsonObject.put("movie_director", rs.getString("director"));
                jsonObject.put("movie_rating", rs.getFloat("rating"));

                // Process genres as hyperlinks to genre browsing
                String genres = rs.getString("genres");
                if (genres != null) {
                    String[] genreList = genres.split(", ");
                    StringBuilder genreLinks = new StringBuilder();
                    for (String gen : genreList) {
                        genreLinks.append("<a href='result.html?genre=").append(gen).append("'>").append(gen).append("</a>, ");
                    }
                    // Remove trailing comma
                    if (genreLinks.length() > 2) {
                        genreLinks.setLength(genreLinks.length() - 2);
                    }
                    jsonObject.put("movie_genres", genreLinks.toString());
                } else {
                    jsonObject.put("movie_genres", "");
                }

                // Process stars as hyperlinks to single star pages
                String stars = rs.getString("stars");
                String star_ids = rs.getString("star_ids");
                if (stars != null && star_ids != null) {
                    String[] starList = stars.split(", ");
                    String[] starIdList = star_ids.split(", ");
                    StringBuilder starLinks = new StringBuilder();
                    for (int i = 0; i < starList.length; i++) {
                        starLinks.append("<a href='single-star.html?id=").append(starIdList[i]).append("'>").append(starList[i]).append("</a>, ");
                    }
                    // Remove trailing comma
                    if (starLinks.length() > 2) {
                        starLinks.setLength(starLinks.length() - 2);
                    }
                    jsonObject.put("movie_stars", starLinks.toString());
                } else {
                    jsonObject.put("movie_stars", "");
                }

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
        } finally {
            out.close();
        }
    }

    // Helper function to get the ORDER based on sortBy
    private String getOrderByClause(String sortBy) {
        switch (sortBy) {
            case "titleAscRatingDesc": return " ORDER BY m.title ASC, r.rating DESC";
            case "titleAscRatingAsc": return " ORDER BY m.title ASC, r.rating ASC";
            case "titleDescRatingDesc": return " ORDER BY m.title DESC, r.rating DESC";
            case "titleDescRatingAsc": return " ORDER BY m.title DESC, r.rating ASC";
            case "ratingDescTitleAsc": return " ORDER BY r.rating DESC, m.title ASC";
            case "ratingDescTitleDesc": return " ORDER BY r.rating DESC, m.title DESC";
            case "ratingAscTitleAsc": return " ORDER BY r.rating ASC, m.title ASC";
            case "ratingAscTitleDesc": return " ORDER BY r.rating ASC, m.title DESC";
            default: return " ORDER BY r.rating DESC, m.title ASC"; // Default sorting
        }
    }
}

