package org.example.carrentalservices;

import SpringProject.dtos.Bookings;
import SpringProject.persistences.BookingDaoImpl;
import SpringProject.persistences.Connector;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookingDaoImplTest {

    @Test
    void addBookingsReturnsTrueWhenInsertSucceeds() throws Exception {
        FakeJdbc jdbc = new FakeJdbc();
        BookingDaoImpl dao = new BookingDaoImpl(jdbc.connector());

        Bookings booking = Bookings.builder()
                .driverId(3)
                .userId(7)
                .carId(11)
                .pickupLocationId(1)
                .dropOffLocationId(2)
                .pickupDateTime(new java.util.Date(1767184800000L))
                .returnDateTime(new java.util.Date(1767357600000L))
                .totalPrice(189.50)
                .status("confirmed")
                .build();

        boolean saved = dao.addBookings(booking);

        assertTrue(saved);
        assertTrue(jdbc.lastSql.contains("INSERT INTO bookings"));
        assertTrue(jdbc.lastSql.contains("(driverId, userId, carId, pickupLocationId, dropOffLocationId, pickupDatetime, returnDatetime, status, totalPrice)"));
        assertTrue(jdbc.lastSql.contains("VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"));
        assertEquals(3, jdbc.lastParameters.get(1));
        assertEquals(7, jdbc.lastParameters.get(2));
        assertEquals(11, jdbc.lastParameters.get(3));
        assertEquals(1, jdbc.lastParameters.get(4));
        assertEquals(2, jdbc.lastParameters.get(5));
        assertEquals(Timestamp.class, jdbc.lastParameters.get(6).getClass());
        assertEquals(Timestamp.class, jdbc.lastParameters.get(7).getClass());
        assertEquals("confirmed", jdbc.lastParameters.get(8));
        assertEquals(189.50, (Double) jdbc.lastParameters.get(9));
    }

    @Test
    void getBookingsByUserIdReturnsOnlyMatchingRow() throws Exception {
        FakeJdbc jdbc = new FakeJdbc();
        Map<String, Object> row = new HashMap<>();
        row.put("bookingId", 42);
        row.put("driverId", 3);
        row.put("userId", 7);
        row.put("carId", 11);
        row.put("pickupLocationId", 1);
        row.put("dropOffLocationId", 2);
        row.put("pickupDatetime", Timestamp.valueOf("2026-05-01 10:00:00"));
        row.put("returnDatetime", Timestamp.valueOf("2026-05-03 10:00:00"));
        row.put("totalPrice", 210.00);
        row.put("status", "confirmed");
        jdbc.queryRows.add(row);

        BookingDaoImpl dao = new BookingDaoImpl(jdbc.connector());
        List<Bookings> bookings = dao.getBookingsByUserId(7);

        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        assertEquals(42, bookings.get(0).getBookingId());
        assertEquals(7, bookings.get(0).getUserId());
        assertEquals(3, bookings.get(0).getDriverId());
    }

    private static final class FakeJdbc {
        private final List<Map<String, Object>> queryRows = new ArrayList<>();
        private String lastSql;
        private final Map<Integer, Object> lastParameters = new HashMap<>();

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
                String name = method.getName();
                if ("getMetaData".equals(name)) {
                    return createMetaData();
                }
                if ("prepareStatement".equals(name)) {
                    lastSql = (String) args[0];
                    return createPreparedStatement();
                }
                if ("getCatalog".equals(name)) {
                    return "test";
                }
                if ("close".equals(name)) {
                    return null;
                }
                if ("isClosed".equals(name)) {
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

        private DatabaseMetaData createMetaData() {
            InvocationHandler handler = (proxy, method, args) -> {
                if ("getColumns".equals(method.getName())) {
                    String columnName = (String) args[3];
                    Map<String, Object> columnRow = new HashMap<>();
                    columnRow.put("COLUMN_NAME", columnName);
                    columnRow.put("IS_NULLABLE", "YES");
                    return createResultSet(List.of(columnRow));
                }
                return defaultValue(method.getReturnType());
            };
            return (DatabaseMetaData) Proxy.newProxyInstance(
                    DatabaseMetaData.class.getClassLoader(),
                    new Class<?>[]{DatabaseMetaData.class},
                    handler
            );
        }

        private PreparedStatement createPreparedStatement() {
            InvocationHandler handler = (proxy, method, args) -> {
                String name = method.getName();
                if ("setInt".equals(name) || "setDouble".equals(name) || "setString".equals(name) || "setTimestamp".equals(name) || "setNull".equals(name)) {
                    lastParameters.put((Integer) args[0], args.length > 1 ? args[1] : null);
                    return null;
                }
                if ("executeUpdate".equals(name)) {
                    return 1;
                }
                if ("executeQuery".equals(name)) {
                    return createResultSet(queryRows);
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

        private ResultSet createResultSet(List<Map<String, Object>> rows) {
            InvocationHandler handler = new InvocationHandler() {
                private int index = -1;

                @Override
                public Object invoke(Object proxy, Method method, Object[] args) {
                    String name = method.getName();
                    if ("next".equals(name)) {
                        index++;
                        return index < rows.size();
                    }
                    if ("getObject".equals(name)) {
                        return currentRow().get(args[0]);
                    }
                    if ("getInt".equals(name)) {
                        Object value = currentRow().get(args[0]);
                        return value == null ? 0 : ((Number) value).intValue();
                    }
                    if ("getDouble".equals(name)) {
                        Object value = currentRow().get(args[0]);
                        return value == null ? 0.0 : ((Number) value).doubleValue();
                    }
                    if ("getString".equals(name)) {
                        Object value = currentRow().get(args[0]);
                        return value == null ? null : value.toString();
                    }
                    if ("getTimestamp".equals(name)) {
                        return currentRow().get(args[0]);
                    }
                    if ("close".equals(name)) {
                        return null;
                    }
                    return defaultValue(method.getReturnType());
                }

                private Map<String, Object> currentRow() {
                    return rows.get(index);
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
