package com.example;

import com.example.utils.RecaptchaVerifyUtils;
import com.google.gson.JsonObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.jasypt.util.password.StrongPasswordEncryptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(name = "DashboardServlet", urlPatterns = "/_dashboard/login")
public class DashboardServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("Starting POST request in DashboardServlet.");

        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        System.out.println("Received gRecaptchaResponse: " + gRecaptchaResponse);

        JsonObject jsonResponse = new JsonObject();

        // Verify reCAPTCHA
        try {
            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
            System.out.println("reCAPTCHA verification passed.");
        } catch (Exception e) {
            System.err.println("reCAPTCHA verification failed: " + e.getMessage());
            jsonResponse.addProperty("status", "fail");
            jsonResponse.addProperty("message", "reCAPTCHA verification failed");
            response.getWriter().write(jsonResponse.toString());
            return;
        }

        String email = request.getParameter("email");
        String password = request.getParameter("password");
        System.out.println("Email: " + email + ", Password: [PROTECTED]");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            System.out.println("Looking up DataSource 'java:comp/env/jdbc/moviedb'");
            InitialContext initialContext = new InitialContext();
            DataSource dataSource = (DataSource) initialContext.lookup("java:comp/env/jdbc/moviedb");
            System.out.println("DataSource lookup successful.");

            try (Connection conn = dataSource.getConnection()) {
                System.out.println("Database connection established.");

                // Check if the user is an employee
                if (!isEmployee(email, conn)) {
                    System.out.println("User " + email + " is not an employee.");
                    jsonResponse.addProperty("status", "fail");
                    jsonResponse.addProperty("message", "Not an employee");
                    response.getWriter().write(jsonResponse.toString());
                    return;
                }
                System.out.println("User " + email + " confirmed as employee.");

                // Validate employee credentials
                if (validateEmployee(email, password, conn)) {
                    System.out.println("User " + email + " authenticated successfully.");
                    request.getSession().setAttribute("employee", email);
                    jsonResponse.addProperty("status", "success");
                } else {
                    System.out.println("Authentication failed for user " + email);
                    jsonResponse.addProperty("status", "fail");
                    jsonResponse.addProperty("message", "Invalid credentials");
                }
                response.getWriter().write(jsonResponse.toString());
            } catch (SQLException e) {
                System.err.println("Database connection error: " + e.getMessage());
                jsonResponse.addProperty("status", "fail");
                jsonResponse.addProperty("message", "Server error. Please try again.");
                response.getWriter().write(jsonResponse.toString());
            }
        } catch (NamingException e) {
            System.err.println("NamingException while looking up DataSource: " + e.getMessage());
            jsonResponse.addProperty("status", "fail");
            jsonResponse.addProperty("message", "Server error. Please try again.");
            response.getWriter().write(jsonResponse.toString());
        }
    }

    private boolean isEmployee(String email, Connection conn) throws SQLException {
        String query = "SELECT 1 FROM employees WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            boolean isEmployee = rs.next();
            System.out.println("Employee check for " + email + ": " + isEmployee);
            return isEmployee;
        }
    }

    private boolean validateEmployee(String email, String password, Connection conn) throws SQLException {
        String query = "SELECT password FROM employees WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String encryptedPassword = rs.getString("password");
                boolean passwordMatches = new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
                System.out.println("Password check for " + email + ": " + passwordMatches);
                return passwordMatches;
            } else {
                System.out.println("No password found for " + email);
            }
        }
        return false;
    }
}
