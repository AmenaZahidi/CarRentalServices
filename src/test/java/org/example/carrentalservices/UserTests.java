package org.example.carrentalservices;
import SpringProject.persistences.Connector;
import SpringProject.persistences.MySqlConnector;
import SpringProject.persistences.UserDao;
import SpringProject.persistences.UserDaoImpl;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class UserTests {
    private static final String TEST_PROPERTIES = "properties/database-test.properties";
    private Connector connector;
    private Connection conn;
    private UserDaoImpl userDao;
    @BeforeEach
     void setUpConnection() throws SQLException {
        Connector connector = new MySqlConnector(TEST_PROPERTIES);
        conn= connector.getConnection();
        conn.setAutoCommit(false);
        userDao = new UserDaoImpl(connector);
    }
    @AfterEach
    void tearDownConnection() throws SQLException {
        conn.rollback();
        connector.freeConnection();
    }
    @Test
void testRegisterSuccess() throws SQLException {
        boolean result = userDao.register("john", "Password123", "john@example.com");
        assertTrue(result);

}

@Test
void testDuplicateUsername() throws SQLException {
        userDao.register("john", "Password123", "john@example.com");
        assertThrows(SQLException.class, ()->userDao.register("john", "Password123", "john@example.com"));
}
@Test
    void testLoginSuccess() throws Exception {
        userDao.register("john", "Password123", "john");
        assertTrue(userDao.login("john", "Password123"));
}
}
