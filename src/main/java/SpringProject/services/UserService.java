package SpringProject.services;

import SpringProject.dtos.User;
import SpringProject.persistences.UserDao;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class UserService {

    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    // Register + Login
    public boolean register(String username, String password, String email, Date dateOfBirth) throws Exception {
        return userDao.register(username, password, email,dateOfBirth);
    }


    public boolean login(String username, String password) throws Exception {
        return userDao.login(username, password);
    }

    // Read users
    public List<String> getUserNames() throws Exception {
        return userDao.getUserNames();
    }

    public User getUserById(int userId) throws Exception {
        return userDao.getUserById(userId);
    }

    public User getUserByUsername(String username) throws Exception {
        return userDao.getUserByUsername(username);
    }

    public User getUserByEmail(String email) throws Exception {
        return userDao.getUserByEmail(email);
    }

    // Update/Delete
    public boolean updateUser(User user) throws Exception {
        return userDao.updateUser(user);
    }

    public boolean deleteUser(int userId) throws Exception {
        return userDao.deleteUser(userId);
    }



    public void closeConnection() throws Exception {
        userDao.closeConnection();
    }
}
