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

import org.json.JSONObject;

@WebServlet(name = "AddToCartServlet", urlPatterns = "/api/addToCart")
public class AddToCartServlet extends HttpServlet {

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Set response content type to JSON
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();

        HttpSession session = request.getSession();
        Integer customerId = (Integer) session.getAttribute("user_id");  // Assuming 'user_id' is stored in session
        String movieId = request.getParameter("movieId");
        int quantity = Integer.parseInt(request.getParameter("quantity"));

        if (customerId == null) {
            jsonResponse.put("status", "fail");
            jsonResponse.put("message", "User is not logged in.");
            out.write(jsonResponse.toString());
            return;
        }

        try (Connection dbCon = dataSource.getConnection()) {
            String query = "INSERT INTO shopping_cart (customer_id, movie_id, quantity) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE quantity = quantity + VALUES(quantity)";

            try (PreparedStatement statement = dbCon.prepareStatement(query)) {
                statement.setInt(1, customerId);
                statement.setString(2, movieId);
                statement.setInt(3, quantity);

                int updateCount = statement.executeUpdate();
                if (updateCount > 0) {
                    jsonResponse.put("status", "success");
                    jsonResponse.put("message", "Item added to cart successfully.");
                } else {
                    jsonResponse.put("status", "fail");
                    jsonResponse.put("message", "Failed to add item to cart.");
                }
            }
        } catch (SQLException e) {
            jsonResponse.put("status", "fail");
            jsonResponse.put("message", "Database error: " + e.getMessage());
            e.printStackTrace();
        }

        out.write(jsonResponse.toString());
        out.close();
    }
}
