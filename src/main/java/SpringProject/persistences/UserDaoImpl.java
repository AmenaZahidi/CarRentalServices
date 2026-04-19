package SpringProject.persistences;


import SpringProject.utils.PasswordHasher;
import SpringProject.dtos.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Repository
@Slf4j
public class UserDaoImpl implements UserDao{
    private  Connector connector;

    public UserDaoImpl(Connector connector){
        this.connector=connector;
    }

    @Override
    public void closeConnection() throws SQLException {
        connector.freeConnection();

    }
    /**
     * Registers a new user in the system.
     * This method validates the provided input, hashes the user's password,
     * and inserts a new record into the users table with a default
     *  userType value.
     *
     * @param username the desired username; must not be null
     * @param password the plaintext password; must not be null or blank
     * @param email    the user's email address; must not be null or blank
     * @return  true if exactly one user was successfully inserted,
     *          false otherwise
     * @throws IllegalArgumentException if any input parameter is invalid
     * @throws SQLException if a database connection cannot be established
     *         or if the insert operation fails
     */
    @Override
    public boolean register(String username, String password, String email) throws SQLException {

        // Validate username
        if (username == null) {
            throw new IllegalArgumentException("Cannot register with a null username!");
        }

        // Validate password
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Cannot register with a null or blank password!");
        }

        // Validate email
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Cannot register with a null or blank email!");
        }

        // Obtain a database connection
        Connection connection = connector.getConnection();
        if (connection == null) {
            throw new SQLException("register(): Could not establish connection to database.");
        }

        // Hash the plaintext password before storing it
        String hashedPassword = PasswordHasher.hashPassword(password);

        int addedRows = 0;

        // Use try-with-resources to ensure the PreparedStatement is closed properly
        // 1. Updated SQL string to include all 6 columns
        String sql = "INSERT INTO users (addressId, username, email, dateOfBirth, password, userType) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setInt(1, 1);                                    // addressId
            ps.setString(2, username);                          // username
            ps.setString(3, email);                             // email
            ps.setDate(4, java.sql.Date.valueOf("2000-01-01")); // dateOfBirth
            ps.setString(5, hashedPassword);                    // password
            ps.setInt(6, 1);                                    // userType

            addedRows = ps.executeUpdate();
        } catch (SQLIntegrityConstraintViolationException e) {
            // Thrown when username or email violates a UNIQUE constraint
            throw new SQLException("Username or email already exists", e);

        } catch (SQLException e) {
            // Log and rethrow any other SQL-related exceptions
            log.error(
                    "register(): The SQL query could not be prepared or executed.\nException: {}",
                    e.getMessage()
            );
            throw e;
        }

        // Registration is successful only if exactly one row was inserted
        return addedRows == 1;
    }


    /**
     * Attempts to authenticate a user using the provided username and password.
     *
     * The method retrieves the user record from the database based on the username,
     * then verifies the supplied password against the stored hashed password.
     *
     * @param username the username supplied by the user
     * @param password the plain-text password supplied by the user
     * @return  true if the username exists and the password is valid;
     *          false if the username does not exist or the password is invalid
     * @throws Exception if a database connection cannot be established
     *                   or if a SQL error occurs during execution
     */
    @Override
    public boolean login(String username, String password) throws Exception {

        // Obtain a database connection
        Connection connection = connector.getConnection();

        // Fail fast if the connection could not be established
        if (connection == null) {
            throw new SQLException(
                    "Could not establish connection to database. Please try again later"
            );
        }

        // Prepare SQL query to retrieve the user by username
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM users WHERE username = ?"
        )) {

            // Bind username parameter to prevent SQL injection
            ps.setString(1, username);

            // Execute query and process result set
            try (ResultSet rs = ps.executeQuery()) {

                // If a user is found, verify the supplied password
                // against the stored hashed password
                if (rs.next()) {
                    String hashedPassword = rs.getString("password");
                    return PasswordHasher.verifyPassword(password, hashedPassword);
                }

                // No user found for the given username
                // Method will return false at the end
            } catch (SQLException e) {
                // Error occurred while executing query or processing the ResultSet
                log.error(
                        "login(): An issue occurred while running the query or processing the ResultSet.\nException: {}",
                        e.getMessage()
                );
                throw e;
            }

        } catch (SQLException e) {
            // Error occurred while preparing the SQL statement
            log.error(
                    "login(): The SQL query could not be prepared.\nException: {}",
                    e.getMessage()
            );
            throw e;
        }

        // Reached when no user exists for the supplied username
        return false;
    }



    /**
     * Retrieves all valid usernames from the database.
     * A username is considered valid if it is:
     *   Not null, Not empty, Not blank (contains non-whitespace characters)
     *
     * @return a List of valid usernames found in the database
     * @throws SQLException if a database connection cannot be established,
     *                      the SQL statement cannot be prepared, or the query fails
     */
    @Override
    public List<String> getUserNames() throws SQLException {

        // Attempt to obtain a database connection
        Connection connection = connector.getConnection();

        // Fail fast if the connection could not be established
        if (connection == null) {
            throw new SQLException(
                    "Could not establish connection to database. Please try again later"
            );
        }

        // List to store retrieved usernames
        List<String> usernames = new ArrayList<>();

        // Prepare SQL statement to fetch non-null and non-empty usernames
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT username FROM users WHERE username IS NOT NULL AND username <> '';"
        )) {

            // Execute query and process the result set
            try (ResultSet rs = ps.executeQuery()) {

                // Iterate through all returned rows
                while (rs.next()) {
                    String username = rs.getString("username");

                    // Additional safety check to avoid blank usernames
                    if (username != null && !username.isBlank()) {
                        usernames.add(username);
                    }
                }

                return usernames;

            } catch (SQLException e) {
                // Error occurred while executing the query or processing the ResultSet
                log.error(
                        "getUserNames(): An issue occurred when running the query or processing " +
                                "the ResultSet.\nException: {}",
                        e.getMessage()
                );
                throw e;
            }

        } catch (SQLException e) {
            // Error occurred while preparing the SQL statement
            log.error(
                    "getUserNames(): The SQL query could not be prepared.\nException: {}",
                    e.getMessage()
            );
            throw e;
        }
    }

    /**
     * retrieves a specified user by its unique user ID
     * @param userId   the id of the user to get
     * @return the matching user or null if there is no match
     * @throws SQLException if the database connection cant be established
     */
    @Override
    public User getUserById(int userId) throws SQLException {
        Connection conn= connector.getConnection();
        if(conn==null){
            throw new SQLException("getUserById(): Could not establish connection to database.");
        }
        User user=null;
        try(PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE userId = ?")){
            ps.setInt(1, userId);
            try(ResultSet rs =ps.executeQuery()){
                if(rs.next()){
                    user= mapUserRow(rs);
                }
            } catch(SQLException e){
                log.error("getUserId(): An issue occurres when running the query or procesing the resultset. " +
                                "\nException: {}",
                        e.getMessage());
                throw e;
            }
        }catch(SQLException e) {
            log.error("getUserById(): the SQL query could not be prepared. \nException: {}", e.getMessage());
            throw e;
        }
        return user;

    }
    /**
     * retrieves a user by specified username
     * @param username   the username of user to get
     * @return the matching user or null if there is no match
     * @throws SQLException if the database connection cant be established
     */
    @Override
    public User getUserByUsername(String username) throws SQLException {
        Connection conn= connector.getConnection();
        if(conn==null){
            throw new SQLException("getUserById(): Could not establish connection to database.");
        }
        User user=null;
        try(PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE username = ?")){
            ps.setString(1, username);
            try(ResultSet rs =ps.executeQuery()){
                if(rs.next()){
                    user= mapUserRow(rs);
                }
            } catch(SQLException e){
                log.error("getUserId(): An issue occurres when running the query or procesing the resultset. " +
                                "\nException: {}",
                        e.getMessage());
                throw e;
            }
        }catch(SQLException e) {
            log.error("getUserById(): the SQL query could not be prepared. \nException: {}", e.getMessage());
            throw e;
        }
        return user;

    }
    /**
     * Retrieves a User from the database using the provided email address.
     * If no user is found with the given email, this method returns  null.
     *
     * @param email the email address of the user to retrieve
     * @return a  User object if a matching record is found;  null otherwise
     * @throws SQLException if a database connection cannot be established
     *         or if an error occurs while preparing or executing the SQL query
     */
    @Override
    public User getUserByEmail(String email) throws SQLException {

        // Obtain a database connection
        Connection conn = connector.getConnection();
        if (conn == null) {
            throw new SQLException("getUserByEmail(): Could not establish connection to database.");
        }

        User user = null;

        // Prepare SQL query to fetch user by email
        try (PreparedStatement ps =
                     conn.prepareStatement("SELECT * FROM users WHERE email = ?")) {

            // Bind email parameter to the prepared statement
            ps.setString(1, email);

            // Execute query and process result set
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Map the current row to a User object
                    user = mapUserRow(rs);
                }
            } catch (SQLException e) {
                // Error while executing query or processing the ResultSet
                log.error(
                        "getUserByEmail(): An issue occurred while executing the query or processing the ResultSet.\nException: {}",
                        e.getMessage()
                );
                throw e;
            }

        } catch (SQLException e) {
            // Error while preparing the SQL statement
            log.error(
                    "getUserByEmail(): The SQL query could not be prepared.\nException: {}",
                    e.getMessage()
            );
            throw e;
        }

        // Return the retrieved user or null if not found
        return user;
    }

    /**
     * updates an existing user record in the database
     * @param user the user that contains the updated values
     * @return true or false if one or more rows were updated, false otherwise
     * @throws SQLException if the sql operation fails
     */
    @Override
    public boolean updateUser(User user) throws SQLException {
        Connection conn=connector.getConnection();
        String sql = "UPDATE users SET username = ?, password = ?, email = ?, userType = ? WHERE userId =?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
            ps.setInt(4,user.getUserType());


            return ps.executeUpdate()>0;
        }catch (SQLException e){
            log.info("updateAlbum(): SQL Exception occurred when attempting to prepare SQL for execution" + e.getMessage());
        }
        return false;
    }
    /**
     * deletes an user from the databse by its id
     * @param userId the id of the user thats being deletes
     * @return true or false if the user is deleted succefully or not
     * @throws SQLException if the sql operation fails.
     */
    @Override
    public boolean deleteUser(int userId) throws SQLException {
        Connection conn = connector.getConnection();
        String sql="DELETE FROM users WHERE userId =?";
        try(PreparedStatement ps = conn.prepareStatement(sql)){
            ps.setInt(1, userId);
            return ps.executeUpdate()>0;
        }catch(SQLException e){
            log.info("deleteAlbum(): SQL Exception occurred when attempting to prepare SQL for execution" + e.getMessage());
        }
        return false;
    }

    private static User mapUserRow(ResultSet rs) throws SQLException{
        return User.builder()
                .userId(rs.getInt("userId"))
                .username(rs.getString("username"))
                .password(rs.getString("password"))
                .email(rs.getString("email"))
                .dateOfBirth(rs.getDate("dateOfBirth"))
                .userType(rs.getInt("userType"))

                .build();
    }

    // TESTING
    public static void main(String [] args) throws SQLException {
        Connector connector = new MySqlConnector("properties/database.properties");
        UserDao userDao = new UserDaoImpl(connector);
        Scanner input = new Scanner(System.in);
        //System.out.println(userDao.getUserNames().toString());
        System.out.print("Username: ");
        String username = input.nextLine().toLowerCase();

        System.out.print("Password: ");
        String password = input.nextLine();

        System.out.print("Email: ");
        String email = input.nextLine();

        try {
            boolean registered = userDao.register(username, password, email);
            if (registered) {
                System.out.println("Welcome to the system, " + username);
            } else {
                System.out.println("Registration failed.");
            }
        }catch(Exception e){
            System.out.println("Username unavailable.");
        }
        /*try {
            boolean loggedin = userDao.login(username, password);
            if (loggedin) {
                System.out.println("Welcome to the system, " + username);
            } else {
                System.out.println("Login failed.");
            }
        }catch(Exception e){
            System.out.println("Username unavailable.");
        }*/

    }



}
