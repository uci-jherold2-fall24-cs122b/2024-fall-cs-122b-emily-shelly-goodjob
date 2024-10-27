import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;

@WebServlet(name = "PlaceOrderServlet", urlPatterns = "/api/placeOrder")
public class PlaceOrderServlet extends HttpServlet {

    private DataSource dataSource;

    public void init() {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        JsonObject jsonResponse = new JsonObject();

        // get user id from session
        HttpSession session = request.getSession();
        Integer customerId = (Integer) session.getAttribute("user_id");

        // check whether user has logged in
        if (customerId == null) {
            jsonResponse.addProperty("status", "fail");
            jsonResponse.addProperty("message", "User not logged in.");
            response.getWriter().write(jsonResponse.toString());
            return;
        }

        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String cardNumber = request.getParameter("creditCardNumber");
        String expirationDate = request.getParameter("expirationDate");

        try (Connection conn = dataSource.getConnection()) {
            String checkCardQuery = "SELECT * FROM creditcards WHERE id = ? AND firstName = ? AND lastName = ? AND expiration = ?";
            try (PreparedStatement stmt = conn.prepareStatement(checkCardQuery)) {
                stmt.setString(1, cardNumber);
                stmt.setString(2, firstName);
                stmt.setString(3, lastName);
                stmt.setDate(4, Date.valueOf(expirationDate));
                ResultSet rs = stmt.executeQuery();

                if (!rs.next()) {
                    jsonResponse.addProperty("status", "fail");
                    jsonResponse.addProperty("message", "Invalid credit card information.");
                    response.getWriter().write(jsonResponse.toString());
                    return;
                }
            }

            String selectCartQuery = "SELECT movie_id, quantity FROM shopping_cart WHERE customer_id = ?";
            try (PreparedStatement cartStmt = conn.prepareStatement(selectCartQuery)) {
                cartStmt.setInt(1, customerId);
                ResultSet cartItems = cartStmt.executeQuery();

                // create query
                String insertSaleQuery = "INSERT INTO sales (customerId, movieId, saleDate, quantity) VALUES (?, ?, ?, ?)";
                try (PreparedStatement saleStmt = conn.prepareStatement(insertSaleQuery)) {
                    conn.setAutoCommit(false);

                    while (cartItems.next()) {
                        String movieId = cartItems.getString("movie_id");
                        int quantity = cartItems.getInt("quantity");

                        saleStmt.setInt(1, customerId);
                        saleStmt.setString(2, movieId);
                        saleStmt.setDate(3, new java.sql.Date(System.currentTimeMillis()));
                        saleStmt.setInt(4, quantity);
                        saleStmt.executeUpdate();
                    }
                    conn.commit(); // Commit transaction
                }

                // removes movies from the shopping cart after successful purchase
                String deleteCartQuery = "DELETE FROM shopping_cart WHERE customer_id = ?";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteCartQuery)) {
                    deleteStmt.setInt(1, customerId);
                    deleteStmt.executeUpdate();
                }
            }

            // response success to frontend
            jsonResponse.addProperty("status", "success");
            jsonResponse.addProperty("message", "Order placed successfully.");
        } catch (SQLException e) {
            jsonResponse.addProperty("status", "fail");
            jsonResponse.addProperty("message", "Error processing transaction: " + e.getMessage());
            e.printStackTrace();
        }
        response.getWriter().write(jsonResponse.toString());
    }
}
