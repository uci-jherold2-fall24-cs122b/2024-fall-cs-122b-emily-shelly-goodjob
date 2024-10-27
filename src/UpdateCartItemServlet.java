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


@WebServlet(name = "UpdateCartItemServlet", urlPatterns = "/api/updateCartItem")
public class UpdateCartItemServlet extends HttpServlet {

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        Integer customerId = (Integer) session.getAttribute("user_id");

        String movieId = request.getParameter("movieId");
        int delta = Integer.parseInt(request.getParameter("delta"));

        try (Connection dbCon = dataSource.getConnection()) {
            String query = "UPDATE shopping_cart SET quantity = quantity + ? WHERE customer_id = ? AND movie_id = ?";
            PreparedStatement statement = dbCon.prepareStatement(query);
            statement.setInt(1, delta);
            statement.setInt(2, customerId);
            statement.setString(3, movieId);

            int updatedRows = statement.executeUpdate();

            if (updatedRows == 0) {
                response.setStatus(404);
                response.getWriter().write("{\"status\": \"fail\", \"message\": \"Item not found.\"}");
            } else {
                response.getWriter().write("{\"status\": \"success\"}");
            }
        } catch (SQLException e) {
            response.setStatus(500);
            response.getWriter().write("{\"status\": \"error\", \"message\": \"Database error.\"}");
        }
    }
}
