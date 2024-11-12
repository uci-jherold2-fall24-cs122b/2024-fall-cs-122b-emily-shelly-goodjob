import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(name = "AddMovieServlet", urlPatterns = "/_dashboard/add_movie")
public class AddMovieServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String title = request.getParameter("movieTitle");
        int year = Integer.parseInt(request.getParameter("year"));
        String director = request.getParameter("director");
        String starName = request.getParameter("starName");
        Integer birthYear = request.getParameter("birthYear").isEmpty() ? null : Integer.parseInt(request.getParameter("birthYear"));
        String genre = request.getParameter("genre");

        JsonObject jsonResponse = new JsonObject();

        try {
            InitialContext initialContext = new InitialContext();
            DataSource dataSource = (DataSource) initialContext.lookup("java:comp/env/jdbc/moviedb");

            try (Connection conn = dataSource.getConnection()) {
                CallableStatement stmt = conn.prepareCall("{CALL add_movie(?, ?, ?, ?, ?, ?)}");
                stmt.setString(1, title);
                stmt.setInt(2, year);
                stmt.setString(3, director);
                stmt.setString(4, starName);
                if (birthYear != null) {
                    stmt.setInt(5, birthYear);
                } else {
                    stmt.setNull(5, java.sql.Types.INTEGER);
                }
                stmt.setString(6, genre);

                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String status = rs.getString("status");
                    String message = rs.getString("message");

                    // Check the status and send the appropriate response
                    jsonResponse.addProperty("status", status);
                    jsonResponse.addProperty("message", message);

                    if ("success".equals(status)) {
                        jsonResponse.addProperty("movie_id", rs.getString("movie_id"));
                        jsonResponse.addProperty("star_id", rs.getString("star_id"));
                        jsonResponse.addProperty("genre_id", rs.getInt("genre_id"));
                    }
                } else {
                    jsonResponse.addProperty("status", "fail");
                    jsonResponse.addProperty("message", "Failed to add the movie.");
                }
            }
        } catch (NamingException | SQLException e) {
            jsonResponse.addProperty("status", "fail");
            jsonResponse.addProperty("message", "Database error.");
            e.printStackTrace();
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }
}