import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import org.json.JSONArray;
import org.json.JSONObject;

@WebServlet(name = "TitleServlet", urlPatterns = "/titles")
public class TitleServlet extends HttpServlet {

    private DataSource dataSource;

    public void init(ServletConfig config) {
        try {
            dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); // Return JSON data
        PrintWriter out = response.getWriter();
        JSONArray jsonArray = new JSONArray();

        // Create title initials list (0-9, A-Z, and "*")
        for (char c = '0'; c <= '9'; c++) {
            jsonArray.put(Character.toString(c));
        }
        for (char c = 'A'; c <= 'Z'; c++) {
            jsonArray.put(Character.toString(c));
        }
        jsonArray.put("*"); // Non-alphanumeric

        out.write(jsonArray.toString());
        out.close();
    }
}
