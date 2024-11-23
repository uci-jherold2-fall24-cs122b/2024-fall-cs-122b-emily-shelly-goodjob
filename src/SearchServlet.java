import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONObject;


@WebServlet(name = "SearchServlet", urlPatterns = "/api/search")
public class SearchServlet extends HttpServlet {
    String jdbcURL = "jdbc:mysql://localhost:3306/moviedb"; // Replace with your database URL
    String dbUser = "root"; // Replace with your database username
    String dbPassword = "goodjob8888"; // Replace with your database password

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String query = request.getParameter("query");
        String[] tokens = query.split(" ");

        StringBuilder ftQuery = new StringBuilder();
        for (String token : tokens) {
            ftQuery.append("+").append(token).append("* ");
        }

        String sql = "SELECT id, title FROM movies WHERE MATCH(title) AGAINST(? IN BOOLEAN MODE)";
        try (Connection conn = DriverManager.getConnection(jdbcURL, dbUser, dbPassword); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ftQuery.toString());
            ResultSet rs = ps.executeQuery();

            JsonArray movieList = new JsonArray();
            while (rs.next()) {
                JsonObject movie = new JsonObject();
                movie.addProperty("id", rs.getString("id"));
                movie.addProperty("title", rs.getString("title"));
                movieList.add(movie);
            }

            response.setContentType("application/json");
            response.getWriter().write(movieList.toString());
        } catch (Exception e) {
            response.sendError(500, e.getMessage());
        }
    }
}

