import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import com.example.utils.RecaptchaVerifyUtils;
import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.NamingException;

@WebServlet(name = "FormReCaptcha", urlPatterns = "/form-recaptcha")
public class FormRecaptcha extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private DataSource dataSource;

    public void init() {
        try {
            InitialContext initialContext = new InitialContext();
            dataSource = (DataSource) initialContext.lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            throw new RuntimeException("Failed to initialize DataSource", e);
        }
    }

    public String getServletInfo() {
        return "Servlet connects to MySQL database and displays result of a SELECT";
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();

        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);

        // Verify reCAPTCHA
        try {
            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
        } catch (Exception e) {
            out.println("<html>");
            out.println("<head><title>Error</title></head>");
            out.println("<body>");
            out.println("<p>recaptcha verification error</p>");
            out.println("<p>" + e.getMessage() + "</p>");
            out.println("</body>");
            out.println("</html>");

            out.close();
            return;
        }

        response.setContentType("text/html"); // Response mime type

        try (Connection dbCon = dataSource.getConnection()) {
            // Retrieve parameter "name" from request, which refers to the value of <input name="name"> in index.html
            String name = request.getParameter("name");

            // Generate a SQL query
            String query = "SELECT * from stars where name LIKE ?";
            PreparedStatement pstmt = dbCon.prepareStatement(query);
            pstmt.setString(1, "%" + name + "%");

            // Perform the query
            ResultSet rs = pstmt.executeQuery();

            // building page head with title
            out.println("<html><head><title>MovieDB: Found Records</title></head>");

            // building page body
            out.println("<body><h1>MovieDB: Found Records</h1>");

            // Create a html <table>
            out.println("<table border>");

            // Iterate through each row of rs and create a table row <tr>
            out.println("<tr><td>ID</td><td>Name</td></tr>");
            while (rs.next()) {
                String m_ID = rs.getString("ID");
                String m_Name = rs.getString("name");
                out.println(String.format("<tr><td>%s</td><td>%s</td></tr>", m_ID, m_Name));
            }
            out.println("</table>");

            out.println("</body></html>");


            // Close all structures
            rs.close();
            pstmt.close();
            dbCon.close();

        } catch (Exception e) {
            out.println("<html>");
            out.println("<head><title>Error</title></head>");
            out.println("<body>");
            out.println("<p>error:</p>");
            out.println("<p>" + e.getMessage() + "</p>");
            out.println("</body>");
            out.println("</html>");

            out.close();
            return;
        }
        out.close();
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }
}
