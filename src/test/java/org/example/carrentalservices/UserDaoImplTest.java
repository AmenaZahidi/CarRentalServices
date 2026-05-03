package org.example.carrentalservices;

import SpringProject.dtos.User;
import SpringProject.persistences.Connector;
import SpringProject.persistences.UserDaoImpl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserDaoImplTest {

    @Test
    void registerStoresAUserRow() throws Exception {
        FakeJdbc jdbc = new FakeJdbc();
        UserDaoImpl dao = new UserDaoImpl(jdbc.connector());

        boolean saved = dao.register("amena", "Secret123!", "amena@example.com");

        assertTrue(saved);
        assertTrue(jdbc.lastSql.contains("INSERT INTO users"));
        assertEquals(1, jdbc.lastParameters.get(1));
        assertEquals("amena", jdbc.lastParameters.get(2));
        assertEquals("amena@example.com", jdbc.lastParameters.get(3));
        assertEquals(1, jdbc.lastParameters.get(6));
        assertNotNull(jdbc.lastParameters.get(5));
        assertNotEquals("Secret123!", jdbc.lastParameters.get(5));
    }

    @Test
    void getUserByIdReturnsMappedUser() throws Exception {
        FakeJdbc jdbc = new FakeJdbc();
        Map<String, Object> row = new HashMap<>();
        row.put("userId", 17);
        row.put("username", "amena");
        row.put("password", "hashed-value");
        row.put("email", "amena@example.com");
        row.put("dateOfBirth", java.sql.Date.valueOf("2001-04-11"));
        row.put("userType", 1);
        jdbc.singleRow = row;

        UserDaoImpl dao = new UserDaoImpl(jdbc.connector());
        User user = dao.getUserById(17);

        assertNotNull(user);
        assertEquals(17, user.getUserId());
        assertEquals("amena", user.getUsername());
        assertEquals("amena@example.com", user.getEmail());
        assertEquals(1, user.getUserType());
    }

    private static final class FakeJdbc {
        private String lastSql;
        private final Map<Integer, Object> lastParameters = new HashMap<>();
        private Map<String, Object> singleRow;

        private Connector connector() {
            return new Connector() {
                private final Connection connection = createConnection();

                @Override
                public Connection getConnection() {
                    return connection;
                }

                @Override
                public void freeConnection() {
                    // no-op
                }
            };
        }

        private Connection createConnection() {
            InvocationHandler handler = (proxy, method, args) -> {
                if ("prepareStatement".equals(method.getName())) {
                    lastSql = (String) args[0];
                    return createPreparedStatement();
                }
                if ("getCatalog".equals(method.getName())) {
                    return "test";
                }
                if ("close".equals(method.getName())) {
                    return null;
                }
                if ("isClosed".equals(method.getName())) {
                    return false;
                }
                return defaultValue(method.getReturnType());
            };
            return (Connection) Proxy.newProxyInstance(
                    Connection.class.getClassLoader(),
                    new Class<?>[]{Connection.class},
                    handler
            );
        }

        private PreparedStatement createPreparedStatement() {
            InvocationHandler handler = (proxy, method, args) -> {
                String name = method.getName();
                if ("setInt".equals(name) || "setDouble".equals(name) || "setString".equals(name) || "setDate".equals(name) || "setTimestamp".equals(name)) {
                    lastParameters.put((Integer) args[0], args[1]);
                    return null;
                }
                if ("executeUpdate".equals(name)) {
                    return 1;
                }
                if ("executeQuery".equals(name)) {
                    return createResultSet(singleRow);
                }
                if ("close".equals(name)) {
                    return null;
                }
                return defaultValue(method.getReturnType());
            };
            return (PreparedStatement) Proxy.newProxyInstance(
                    PreparedStatement.class.getClassLoader(),
                    new Class<?>[]{PreparedStatement.class},
                    handler
            );
        }

        private ResultSet createResultSet(Map<String, Object> row) {
            InvocationHandler handler = new InvocationHandler() {
                private boolean read;

                @Override
                public Object invoke(Object proxy, Method method, Object[] args) {
                    String name = method.getName();
                    if ("next".equals(name)) {
                        if (read || row == null) {
                            return false;
                        }
                        read = true;
                        return true;
                    }
                    if ("getObject".equals(name) || "getString".equals(name) || "getInt".equals(name) || "getDate".equals(name)) {
                        Object value = row.get(args[0]);
                        if ("getInt".equals(name)) {
                            return value == null ? 0 : ((Number) value).intValue();
                        }
                        return value;
                    }
                    if ("close".equals(name)) {
                        return null;
                    }
                    return defaultValue(method.getReturnType());
                }
            };
            return (ResultSet) Proxy.newProxyInstance(
                    ResultSet.class.getClassLoader(),
                    new Class<?>[]{ResultSet.class},
                    handler
            );
        }

        private Object defaultValue(Class<?> type) {
            if (type == boolean.class) return false;
            if (type == byte.class) return (byte) 0;
            if (type == short.class) return (short) 0;
            if (type == int.class) return 0;
            if (type == long.class) return 0L;
            if (type == float.class) return 0f;
            if (type == double.class) return 0d;
            return null;
        }
    }
}
