import java.sql.*;
import java.util.ArrayList;

import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class UpdateEmployeePassword {

    /*
     *
     * This program updates your existing employees table to change the
     * plain text passwords to encrypted passwords.
     *
     * You should only run this program **once**, because this program uses the
     * existing passwords as real passwords, then replace them. If you run it more
     * than once, it will treat the encrypted passwords as real passwords and
     * generate wrong values.
     *
     */
    public static void main(String[] args) throws Exception {

        DataSource dataSource;
        try {
            // Initialize the DataSource from JNDI
            InitialContext context = new InitialContext();
            dataSource = (DataSource) context.lookup("java:comp/env/jdbc/moviedb");
        } catch (NamingException e) {
            System.err.println("Failed to initialize DataSource.");
            e.printStackTrace();
            return;
        }

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            // change the employees table password column from VARCHAR(20) to VARCHAR(128)
            String alterQuery = "ALTER TABLE employees MODIFY COLUMN password VARCHAR(128)";
            int alterResult = statement.executeUpdate(alterQuery);
            System.out.println("Altering employees table schema completed, " + alterResult + " rows affected");

            // get the email and password for each employee
            String query = "SELECT email, password FROM employees";

            try (PreparedStatement selectStmt = connection.prepareStatement(query);
                 ResultSet rs = selectStmt.executeQuery()) {

                // we use the StrongPasswordEncryptor from jasypt library (Java Simplified Encryption)
                PasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();

                ArrayList<String> updateQueryList = new ArrayList<>();

                System.out.println("Encrypting passwords (this might take a while)");
                while (rs.next()) {
                    // get the email and plain text password from the current table
                    String email = rs.getString("email");
                    String password = rs.getString("password");

                    // encrypt the password using StrongPasswordEncryptor
                    String encryptedPassword = passwordEncryptor.encryptPassword(password);

                    // generate the update query
                    String updateQuery = String.format("UPDATE employees SET password='%s' WHERE email='%s';", encryptedPassword, email);
                    updateQueryList.add(updateQuery);
                }

                // execute the update queries to update the passwords
                System.out.println("Updating passwords");
                int count = 0;
                for (String updateQuery : updateQueryList) {
                    int updateResult = statement.executeUpdate(updateQuery);
                    count += updateResult;
                }
                System.out.println("Updating passwords completed, " + count + " rows affected");
            }
        } catch (Exception e) {
            System.err.println("Error occurred while updating passwords.");
            e.printStackTrace();
        }

        System.out.println("Finished");
    }

}