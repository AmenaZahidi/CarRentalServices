package SpringProject.persistences;

import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Slf4j
public class MySqlConnector implements Connector{
    private Connection connection;
    private Properties properties;

    public MySqlConnector(String propertiesFilename) {
       properties = new Properties();
        try {
            InputStream in = Thread.currentThread()
                    .getContextClassLoader()
                    .getResourceAsStream(propertiesFilename);

            if (in == null) {
                throw new RuntimeException("Could not find resource: " + propertiesFilename);
            }

            properties.load(in);
            log.info("Loaded database properties from {}", propertiesFilename);
        } catch (Exception e) {
            System.out.println("An exception occurred when attempting to load properties from \"" + propertiesFilename + "\": " + e.getMessage());

            log.error("Error: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        connection = null;

        String driver = properties.getProperty("driver", "com.mysql.cj.jdbc.Driver");
        String url = properties.getProperty("url", "jdbc:mysql://127.0.0.1:3306/");
        String database = properties.getProperty("database", "carRentaldb");
        String username = properties.getProperty("username", "root");
        String password = properties.getProperty("password", "");

        try{
            //load the database driver
            Class.forName(driver);
            connection = DriverManager.getConnection(url+database, username, password);
        } catch (ClassNotFoundException e) {
            log.error("Connection could not be established - incorrect URL or database not switched on. \n Exception: {}", e.getMessage());
        } catch (SQLException e) {
          log.error("Driver files have not been loaded. Please check pom driver dependencies details. \n Exception:" +
                  " {}", e.getMessage());
        }
        return connection;
    }

    public void freeConnection() {
    if(connection != null){
        try{
            connection.close();
            connection = null;
        } catch (SQLException e) {
            log.error("An exception occurred when attempting to close the connection to the database \n " +
                    "Exception:" +
                    " {}", e.getMessage());
        }
    }
    }

}
