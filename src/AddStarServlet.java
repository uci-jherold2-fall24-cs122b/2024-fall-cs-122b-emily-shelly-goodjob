import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;

@WebServlet(name = "AddStarServlet", urlPatterns = "/_dashboard/add_star")
public class AddStarServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String starName = request.getParameter("startname");
        String birthYear = request.getParameter("birthYear");

        JsonObject jsonResponse = new JsonObject();

        // Validate star name
        if (starName == null || starName.trim().isEmpty()) {
            jsonResponse.addProperty("status", "fail");
            jsonResponse.addProperty("message", "Star name is required.");
            response.getWriter().write(jsonResponse.toString());
            return;
        }

        try {
            InitialContext initialContext = new InitialContext();
            DataSource dataSource = (DataSource) initialContext.lookup("java:comp/env/jdbc/moviedb");

            try (Connection conn = dataSource.getConnection()) {
                String newStarId = generateNextStarId(conn); // Generate new ID

                if (newStarId == null) {
                    jsonResponse.addProperty("status", "fail");
                    jsonResponse.addProperty("message", "Failed to generate star ID.");
                    response.getWriter().write(jsonResponse.toString());
                    return;
                }

                // Insert new star with generated ID
// Insert new star with generated ID
                String insertQuery = "INSERT INTO stars (id, name, birthYear) VALUES (?, ?, ?)";
                try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                    stmt.setString(1, newStarId);
                    stmt.setString(2, starName);
                    if (birthYear != null && !birthYear.trim().isEmpty()) {
                        stmt.setInt(3, Integer.parseInt(birthYear));
                    } else {
                        stmt.setNull(3, java.sql.Types.INTEGER);
                    }

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        jsonResponse.addProperty("status", "success");
                        jsonResponse.addProperty("message", "Success! Star ID: " + newStarId);

                        // Add a print statement to confirm the successful insertion
                        System.out.println("Inserted star: " + starName + " with ID: " + newStarId);
                    } else {
                        jsonResponse.addProperty("status", "fail");
                        jsonResponse.addProperty("message", "Failed to add the star.");
                    }
                }
            }
        } catch (NamingException | SQLException e) {
            jsonResponse.addProperty("status", "fail");
            jsonResponse.addProperty("message", "Database error.");
            e.printStackTrace();
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(jsonResponse.toString());
    }

    private String generateNextStarId(Connection conn) throws SQLException {
        String newId = null;
        conn.setAutoCommit(false); // Begin transaction

        try {
            // Lock both `star_id_helper` and `stars` tables for consistency
            try (PreparedStatement lockStmt = conn.prepareStatement("LOCK TABLES star_id_helper WRITE, stars WRITE")) {
                lockStmt.execute();
            }

            // Retrieve the last used star ID from `star_id_helper`
            String currentId = null;
            try (PreparedStatement getIdStmt = conn.prepareStatement("SELECT last_id FROM star_id_helper")) {
                ResultSet rs = getIdStmt.executeQuery();
                if (rs.next()) {
                    currentId = rs.getString("last_id");
                }
            }

            // Increment the ID if it's retrieved
            if (currentId != null) {
                String prefix = currentId.substring(0, 2); // "nm"
                int numericPart = Integer.parseInt(currentId.substring(2)); // e.g., "9423081" to 9423081

                // Loop to find the next available ID
                boolean idExists;
                do {
                    numericPart++; // Increment for the new ID
                    newId = prefix + numericPart;

                    // Check if this ID already exists in the `stars` table
                    try (PreparedStatement checkStmt = conn.prepareStatement("SELECT 1 FROM stars WHERE id = ?")) {
                        checkStmt.setString(1, newId);
                        try (ResultSet checkRs = checkStmt.executeQuery()) {
                            idExists = checkRs.next();
                        }
                    }
                } while (idExists); // Continue if the ID already exists

                // Update the last_id in the helper table
                try (PreparedStatement updateStmt = conn.prepareStatement("UPDATE star_id_helper SET last_id = ?")) {
                    updateStmt.setString(1, newId);
                    updateStmt.executeUpdate();
                }
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            // Unlock the tables
            try (PreparedStatement unlockStmt = conn.prepareStatement("UNLOCK TABLES")) {
                unlockStmt.execute();
            }
            conn.setAutoCommit(true);
        }
        return newId;
    }

}
