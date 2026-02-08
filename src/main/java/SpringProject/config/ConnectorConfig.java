package SpringProject.config;


import SpringProject.persistences.Connector;
import SpringProject.persistences.MySqlConnector;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.sql.SQLException;

@Configuration
public class ConnectorConfig {
    @Bean
    public Connector connector() {
        return new MySqlConnector("properties/database.properties");
    }
}

