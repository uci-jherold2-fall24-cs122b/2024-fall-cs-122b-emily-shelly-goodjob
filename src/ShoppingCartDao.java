import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class ShoppingCartDao {
    private static DataSource dataSource;

    static {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    private static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void addToCart(int customerId, String movieId, int quantity) throws SQLException {
        String query = "INSERT INTO shopping_cart (customer_id, movie_id, quantity) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE quantity = quantity + ?";

        try (Connection conn = getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, customerId);
            statement.setString(2, movieId);
            statement.setInt(3, quantity);
            statement.setInt(4, quantity);

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error adding to cart", e);
        }
    }

    public static void updateCart(int cartId, int newQuantity) throws SQLException {
        String query = "UPDATE shopping_cart SET quantity = ? WHERE cart_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, newQuantity);
            statement.setInt(2, cartId);

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error updating cart", e);
        }
    }

    public static void removeFromCart(int cartId) throws SQLException {
        String query = "DELETE FROM shopping_cart WHERE cart_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement statement = conn.prepareStatement(query)) {
            statement.setInt(1, cartId);

            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SQLException("Error removing from cart", e);
        }
    }
}
