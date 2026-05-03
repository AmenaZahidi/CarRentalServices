package org.example.carrentalservices;

import SpringProject.dtos.Payment;
import SpringProject.persistences.Connector;
import SpringProject.persistences.PaymentDaoImpl;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentDaoImplTest {

    @Test
    void addPaymentWritesAllFieldsWhenColumnsExist() throws Exception {
        FakeJdbc jdbc = new FakeJdbc();
        PaymentDaoImpl dao = new PaymentDaoImpl(jdbc.connector());

        Payment payment = Payment.builder()
                .bookingId(44)
                .amount(259.99)
                .paymentStatus("paid")
                .transactionRef("TXN-001")
                .build();

        boolean saved = dao.addPayment(payment);

        assertTrue(saved);
        assertTrue(jdbc.lastSql.contains("INSERT INTO payment"));
        assertEquals(44, jdbc.lastParameters.get(1));
        assertEquals(259.99, (Double) jdbc.lastParameters.get(2));
        assertEquals("paid", jdbc.lastParameters.get(3));
        assertEquals("TXN-001", jdbc.lastParameters.get(4));
    }

    @Test
    void getPaymentByBookingIdReturnsMappedPayment() throws Exception {
        FakeJdbc jdbc = new FakeJdbc();
        Map<String, Object> row = new HashMap<>();
        row.put("paymentId", 8);
        row.put("bookingId", 44);
        row.put("paymentDate", Timestamp.valueOf("2026-05-03 11:15:00"));
        row.put("amount", 259.99);
        row.put("paymentStatus", "paid");
        row.put("transactionRef", "TXN-001");
        jdbc.queryRows.add(row);

        PaymentDaoImpl dao = new PaymentDaoImpl(jdbc.connector());
        Payment payment = dao.getPaymentByBookingId(44);

        assertNotNull(payment);
        assertEquals(8, payment.getPaymentId());
        assertEquals(44, payment.getBookingId());
        assertEquals(259.99, payment.getAmount());
        assertEquals("paid", payment.getPaymentStatus());
        assertEquals("TXN-001", payment.getTransactionRef());
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
                if ("setInt".equals(name) || "setDouble".equals(name) || "setString".equals(name)) {
                    lastParameters.put((Integer) args[0], args[1]);
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
