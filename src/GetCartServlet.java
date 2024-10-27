import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet(name = "GetCartServlet", urlPatterns = "/api/getCart")
public class GetCartServlet extends HttpServlet {

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession();
        Integer customerId = (Integer) session.getAttribute("user_id");

        try (Connection dbCon = dataSource.getConnection()) {
            String query = "SELECT m.title, sc.movie_id, sc.quantity, m.price " +
                    "FROM shopping_cart sc " +
                    "JOIN movies m ON sc.movie_id = m.id " +
                    "WHERE sc.customer_id = ?";
            PreparedStatement statement = dbCon.prepareStatement(query);
            statement.setInt(1, customerId);

            ResultSet rs = statement.executeQuery();
            JSONArray cartItems = new JSONArray();

            while (rs.next()) {
                JSONObject item = new JSONObject();
                item.put("title", rs.getString("title"));
                item.put("movieId", rs.getString("movie_id"));
                item.put("quantity", rs.getInt("quantity"));
                item.put("price", rs.getDouble("price"));
                cartItems.put(item);
            }

            out.write(cartItems.toString());
        } catch (SQLException e) {
            response.setStatus(500);
            out.write("{\"error\": \"Failed to retrieve cart items.\"}");
        }
        out.close();
    }
}
