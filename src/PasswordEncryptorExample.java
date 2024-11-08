import org.jasypt.util.password.StrongPasswordEncryptor;

public class PasswordEncryptorExample {
    public static void main(String[] args) {
        String plainTextPassword = "classta";
        StrongPasswordEncryptor passwordEncryptor = new StrongPasswordEncryptor();
        String encryptedPassword = passwordEncryptor.encryptPassword(plainTextPassword);

        System.out.println("Encrypted password: " + encryptedPassword);
    }
}
