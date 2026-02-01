package SpringProject.utils;

/******************************************
 * @Author: Julie Olamijuwon              *
 * @StudentID: D00215779                  *
 * @Date:   January 2026                  *
 ******************************************/

import SpringProject.persistences.Connector;
import SpringProject.persistences.MySqlConnector;
import SpringProject.persistences.UserDao;
import SpringProject.persistences.UserDaoImpl;
import org.mindrot.jbcrypt.BCrypt;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
public class PasswordHasher {
    // Define the BCrypt workload to use when generating password hashes.
    // 10-31 is a valid value.
    private static final int WORKLOAD = 12;

    /**
     * Hashes a supplied plaintext password using BCrypt
     * This automatically handles secure 128-bit salt generation and storage within the hash.
     * @param plaintext The account's password in plaintext form.
     * @return A string of length 60 containing the bcrypt hashed password in crypt(3) format - $id$salt$hash.
     * @throws IllegalArgumentException where supplied string is null or empty.
     */
    public static String hashPassword(String plaintext) {
        if(plaintext == null || plaintext.isEmpty()){
            throw new IllegalArgumentException("Cannot hash a null or empty string");
        }


        String salt = BCrypt.gensalt(WORKLOAD);
        return BCrypt.hashpw(plaintext, salt);
    }

    /**
     * Verifies a supplied plaintext password against a supplied hash value
     * @param plaintext The password to verify
     * @param hashedPassword The previously generated hash of the password
     * @return boolean - true if the password matches the hash, false otherwise (including if
     * either value is null)
     */
    public static boolean verifyPassword(String plaintext, String hashedPassword) {
        if(plaintext == null || hashedPassword == null || hashedPassword.isEmpty()){
            return false;
        }

        return BCrypt.checkpw(plaintext, hashedPassword);
    }

    public static void main(String[] args) throws SQLException {
        Scanner input = new Scanner(System.in);
        System.out.println("Enter username: ");
        String enteredUsername = input.nextLine().toLowerCase();
        System.out.println("Enter password: ");
        String enteredPassword = input.nextLine();

        boolean loggedIn = testWithHardCodedValues(enteredUsername, enteredPassword);
        //boolean loggedIn = testWithDatabase(enteredUsername, enteredPassword);
        if(loggedIn){
            System.out.println("Login successful!");
        }else{
            System.out.println("Incorrect credentials entered");
        }
    }

    private static boolean testWithDatabase(String enteredUsername, String enteredPassword) throws Exception {
        Connector connector = new MySqlConnector("properties/database.properties");
        UserDao userDao = new UserDaoImpl(connector);
        boolean loggedIn = userDao.login(enteredUsername, enteredPassword);
        return loggedIn;
    }

    private static boolean testWithHardCodedValues(String enteredUsername, String enteredPassword){
        List<String> usernames = List.of("athena", "helo", "lee", "kara", "laura");
        List<String> passwords = List.of("password", "Passw0rd!", "helloThere01?", "AccessGr4nted!",
                "Secure PassPhrase1!");

        List<String> hashes = new ArrayList<String>();

        for (int i = 0; i < passwords.size(); i++) {
            String currentPasswordHash = PasswordHasher.hashPassword(passwords.get(i));
            hashes.add(currentPasswordHash);
        }

        int credentialIndex = usernames.indexOf(enteredUsername);
        if(credentialIndex != -1){
            String hashed = hashes.get(credentialIndex);
            return PasswordHasher.verifyPassword(enteredPassword, hashed);
        }
        return false;
    }
}
