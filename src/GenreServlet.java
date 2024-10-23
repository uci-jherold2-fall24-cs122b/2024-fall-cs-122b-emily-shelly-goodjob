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

@WebServlet(name = "GenreServlet", urlPatterns = "/genres")
public class GenreServlet extends HttpServlet {

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

        try {
            // Create the SQL query to fetch all genres
            String query = "SELECT name FROM genres ORDER BY name ASC";

            // Create a new connection to the database
            Connection dbCon = dataSource.getConnection();
            PreparedStatement statement = dbCon.prepareStatement(query);

            ResultSet rs = statement.executeQuery();

            // Convert result set to JSON
            while (rs.next()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("genre_name", rs.getString("name"));
                jsonArray.put(jsonObject);
            }

            // Output JSON
            out.write(jsonArray.toString());

            rs.close();
            statement.close();
            dbCon.close();

        } catch (SQLException e) {
            request.getServletContext().log("SQL Error: ", e);
            out.write("{\"error\": \"SQL error in doGet: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            request.getServletContext().log("General Error: ", e);
            out.write("{\"error\": \"Error in doGet: " + e.getMessage() + "\"}");
        }
        out.close();
    }
}
