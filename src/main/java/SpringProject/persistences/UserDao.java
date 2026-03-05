package SpringProject.persistences;
/******************************************
 * @Author: Julie Olamijuwon              *
 * @StudentID: D00215779                  *
 * @Date:   January 2026                  *
 ******************************************/


import SpringProject.dtos.User;
import java.sql.SQLException;
import java.util.List;

public interface UserDao {
    boolean register(String username, String password, String email)throws Exception;

    //boolean register(String username, String password, String email, Date dateOfBirth) throws SQLException;

    boolean login(String username, String password)throws Exception;
    List<String> getUserNames()throws SQLException;
    User getUserById(int userId)throws SQLException;
    User getUserByUsername(String username) throws SQLException;
    User getUserByEmail(String email) throws SQLException;
    boolean updateUser(User user)throws SQLException;
    boolean deleteUser(int userId) throws SQLException;
    void closeConnection() throws SQLException;
}
