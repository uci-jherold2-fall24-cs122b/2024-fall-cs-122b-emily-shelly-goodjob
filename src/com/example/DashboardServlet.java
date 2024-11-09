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
        String gRecaptchaResponse = request.getParameter("g-recaptcha-response");
        System.out.println("gRecaptchaResponse=" + gRecaptchaResponse);

        JsonObject jsonResponse = new JsonObject();

        // Verify reCAPTCHA
        try {
            RecaptchaVerifyUtils.verify(gRecaptchaResponse);
        } catch (Exception e) {
            jsonResponse.addProperty("status", "fail");
            jsonResponse.addProperty("message", "reCAPTCHA verification failed");
            response.getWriter().write(jsonResponse.toString());
            return; // Exit if reCAPTCHA fails
        }

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try {
            InitialContext initialContext = new InitialContext();
            DataSource dataSource = (DataSource) initialContext.lookup("java:comp/env/jdbc/moviedb");

            try (Connection conn = dataSource.getConnection()) {
                if (!isEmployee(email, conn)) {
                    jsonResponse.addProperty("status", "fail");
                    jsonResponse.addProperty("message", "Not an employee");
                    response.getWriter().write(jsonResponse.toString());
                    return;
                }

                if (validateEmployee(email, password, conn)) {
                    request.getSession().setAttribute("employee", email);
                    jsonResponse.addProperty("status", "success");
                } else {
                    jsonResponse.addProperty("status", "fail");
                    jsonResponse.addProperty("message", "Invalid credentials");
                }
                response.getWriter().write(jsonResponse.toString());
            }
        } catch (NamingException | SQLException e) {
            log("Error in DashboardServlet during POST request: ", e);
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
            return rs.next();
        }
    }

    private boolean validateEmployee(String email, String password, Connection conn) throws SQLException {
        String query = "SELECT password FROM employees WHERE email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String encryptedPassword = rs.getString("password");
                return new StrongPasswordEncryptor().checkPassword(password, encryptedPassword);
            }
        }
        return false;
    }
}
