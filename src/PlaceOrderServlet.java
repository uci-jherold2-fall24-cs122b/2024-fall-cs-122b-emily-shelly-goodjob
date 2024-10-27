import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
                    conn.commit();
                }

                // removes movies from the shopping cart after successful purchase
                String clearCartQuery = "DELETE FROM shopping_cart WHERE customer_id = ?";
                try (PreparedStatement clearCartStmt = conn.prepareStatement(clearCartQuery)) {
                    clearCartStmt.setInt(1, customerId);
                    clearCartStmt.executeUpdate();
                }
            }

            // response success to frontend
            jsonResponse.addProperty("status", "success");
            jsonResponse.addProperty("message", "Order placed successfully.");

            JSONArray salesArray = new JSONArray();
            String salesQuery = "SELECT s.id AS sale_id, m.title, sc.quantity, m.price " +
                    "FROM sales s " +
                    "JOIN movies m ON s.movieId = m.id " +
                    "JOIN shopping_cart sc ON sc.movie_id = s.movieId " +
                    "WHERE s.customerId = ?";

            try (PreparedStatement salesStmt = conn.prepareStatement(salesQuery)) {
                salesStmt.setInt(1, customerId);
                ResultSet salesResult = salesStmt.executeQuery();

                while (salesResult.next()) {
                    JSONObject sale = new JSONObject();
                    sale.put("saleId", salesResult.getInt("sale_id"));
                    sale.put("title", salesResult.getString("title"));
                    sale.put("quantity", salesResult.getInt("quantity"));
                    sale.put("price", salesResult.getBigDecimal("price"));
                    sale.put("total", salesResult.getBigDecimal("price")
                            .multiply(BigDecimal.valueOf(salesResult.getInt("quantity"))));

                    salesArray.put(sale);
                }
            }

            // convert JSONArray to JsonElement and add to jsonResponse
            String salesArrayString = salesArray.toString();
            JsonElement salesJsonElement = JsonParser.parseString(salesArrayString);
            jsonResponse.add("sales", salesJsonElement);

        } catch (SQLException e) {
            jsonResponse.addProperty("status", "fail");
            jsonResponse.addProperty("message", "Error processing transaction: " + e.getMessage());
            e.printStackTrace();
        }
        response.getWriter().write(jsonResponse.toString());
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        JsonObject jsonResponse = new JsonObject();

        HttpSession session = request.getSession();
        Integer customerId = (Integer) session.getAttribute("user_id");

        if (customerId == null) {
            jsonResponse.addProperty("status", "fail");
            jsonResponse.addProperty("message", "User not logged in.");
            response.getWriter().write(jsonResponse.toString());
            return;
        }

        try (Connection conn = dataSource.getConnection()) {
            JSONArray salesArray = new JSONArray();
            String salesQuery = "SELECT s.id AS sale_id, m.title, sc.quantity, m.price " +
                    "FROM sales s " +
                    "JOIN movies m ON s.movieId = m.id " +
                    "JOIN shopping_cart sc ON sc.movie_id = s.movieId " +
                    "WHERE s.customerId = ?";

            try (PreparedStatement salesStmt = conn.prepareStatement(salesQuery)) {
                salesStmt.setInt(1, customerId);
                ResultSet salesResult = salesStmt.executeQuery();

                while (salesResult.next()) {
                    JSONObject sale = new JSONObject();
                    sale.put("saleId", salesResult.getInt("sale_id"));
                    sale.put("title", salesResult.getString("title"));
                    sale.put("quantity", salesResult.getInt("quantity"));
                    sale.put("price", salesResult.getBigDecimal("price"));
                    sale.put("total", salesResult.getBigDecimal("price")
                            .multiply(BigDecimal.valueOf(salesResult.getInt("quantity"))));

                    salesArray.put(sale);
                }
            }

            // Convert JSONArray to JsonElement and add to jsonResponse
            String salesArrayString = salesArray.toString();
            JsonElement salesJsonElement = JsonParser.parseString(salesArrayString);
            jsonResponse.add("sales", salesJsonElement);
            jsonResponse.addProperty("status", "success");

        } catch (SQLException e) {
            jsonResponse.addProperty("status", "fail");
            jsonResponse.addProperty("message", "Error retrieving sales data: " + e.getMessage());
            e.printStackTrace();
        }
        response.getWriter().write(jsonResponse.toString());
    }
}
