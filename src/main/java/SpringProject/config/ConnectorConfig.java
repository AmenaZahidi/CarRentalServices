package SpringProject.config;


import SpringProject.persistences.Connector;
import SpringProject.persistences.MySqlConnector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.sql.SQLException;

@Configuration
public class ConnectorConfig {
   private Environment env;

   public ConnectorConfig(Environment env) {
       this.env = env;
    }

    @Bean
    public Connector connector() throws SQLException {
       String path = env.getProperty("connector.properties.path");
       return new MySqlConnector(path);
    }

}
