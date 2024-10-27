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
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();

        HttpSession session = request.getSession();
        Integer customerId = (Integer) session.getAttribute("user_id");  // Assuming 'user_id' is set in session during login
        String movieId = request.getParameter("movieId");

        if (customerId == null) {
            jsonResponse.put("status", "fail");
            jsonResponse.put("message", "User is not logged in.");
            out.write(jsonResponse.toString());
            out.close();
            return;
        }

        try (Connection dbCon = dataSource.getConnection()) {
            String checkQuery = "SELECT quantity FROM shopping_cart WHERE customer_id = ? AND movie_id = ?";
            String updateQuery = "UPDATE shopping_cart SET quantity = quantity + 1 WHERE customer_id = ? AND movie_id = ?";
            String insertQuery = "INSERT INTO shopping_cart (customer_id, movie_id, quantity) VALUES (?, ?, 1)";

            try (PreparedStatement checkStmt = dbCon.prepareStatement(checkQuery)) {
                checkStmt.setInt(1, customerId);
                checkStmt.setString(2, movieId);

                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    // Movie already in cart, increment quantity
                    try (PreparedStatement updateStmt = dbCon.prepareStatement(updateQuery)) {
                        updateStmt.setInt(1, customerId);
                        updateStmt.setString(2, movieId);
                        updateStmt.executeUpdate();
                    }
                    jsonResponse.put("status", "success");
                    jsonResponse.put("message", "Quantity updated in cart.");
                } else {
                    // Movie not in cart, insert new entry with quantity = 1
                    try (PreparedStatement insertStmt = dbCon.prepareStatement(insertQuery)) {
                        insertStmt.setInt(1, customerId);
                        insertStmt.setString(2, movieId);
                        insertStmt.executeUpdate();
                    }
                    jsonResponse.put("status", "success");
                    jsonResponse.put("message", "Item added to cart.");
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
