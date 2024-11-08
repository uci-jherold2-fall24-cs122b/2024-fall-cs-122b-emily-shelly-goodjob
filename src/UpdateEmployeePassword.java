import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.jasypt.util.password.PasswordEncryptor;
import org.jasypt.util.password.StrongPasswordEncryptor;

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

        String loginUser = "mytestuser";
        String loginPasswd = "My6$Password";
        String loginUrl = "jdbc:mysql://localhost:3306/moviedb";

        Class.forName("com.mysql.jdbc.Driver").newInstance();
        Connection connection = DriverManager.getConnection(loginUrl, loginUser, loginPasswd);
        Statement statement = connection.createStatement();

        // change the employees table password column from VARCHAR(20) to VARCHAR(128)
        String alterQuery = "ALTER TABLE employees MODIFY COLUMN password VARCHAR(128)";
        int alterResult = statement.executeUpdate(alterQuery);
        System.out.println("Altering employees table schema completed, " + alterResult + " rows affected");

        // get the email and password for each employee
        String query = "SELECT email, password FROM employees";

        ResultSet rs = statement.executeQuery(query);

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
        rs.close();

        // execute the update queries to update the passwords
        System.out.println("Updating passwords");
        int count = 0;
        for (String updateQuery : updateQueryList) {
            int updateResult = statement.executeUpdate(updateQuery);
            count += updateResult;
        }
        System.out.println("Updating passwords completed, " + count + " rows affected");

        statement.close();
        connection.close();

        System.out.println("Finished");

    }

}