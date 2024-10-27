import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.naming.InitialContext;

@WebServlet(name = "LoginServlet", urlPatterns = "/api/login")
public class LoginServlet extends HttpServlet {
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        /* This example only allows username/password to be test/test
        /  in the real project, you should talk to the database to verify username/password
        */
        JsonObject responseJsonObject = new JsonObject();
        try {
            InitialContext initialContext = new InitialContext();
            DataSource dataSource = (DataSource) initialContext.lookup("java:comp/env/jdbc/moviedb");
            try (Connection conn = dataSource.getConnection()) {
                if (!userExists(username, conn)) {
                    responseJsonObject.addProperty("status", "fail");
                    responseJsonObject.addProperty("message", "Username does not exist");
                } else {
                    if (validateUser(username, password, conn)) {
                        request.getSession().setAttribute("user", new User(username));
                        responseJsonObject.addProperty("status", "success");
                        responseJsonObject.addProperty("message", "Log in successfully");
                    } else {
                        responseJsonObject.addProperty("status", "fail");
                        responseJsonObject.addProperty("message", "Password does not match");
                    }
                }
            }
        } catch (NamingException | SQLException e) {
            e.printStackTrace();
            responseJsonObject.addProperty("status", "fail");
            responseJsonObject.addProperty("message", "Errors occurred while logging in");
        }
        response.getWriter().write(responseJsonObject.toString());
    }

    private boolean userExists(String email, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT id FROM customers WHERE email = ?")) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    private boolean validateUser(String email, String password, Connection conn) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT id FROM customers WHERE email = ? AND password = ?")) {
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }
}
