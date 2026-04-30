package SpringProject.persistences;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
@Slf4j
public class DriverLicenceDaoImpl implements DriverLicenceDao {
    private final Connector connector;

    public DriverLicenceDaoImpl(Connector connector) {
        this.connector = connector;
    }

    @Override
    public boolean saveLicenceProof(int driverId,
                                    String licenseNumber,
                                    String permitType,
                                    String proofPath) throws SQLException {
        Connection conn = connector.getConnection();
        if (conn == null) throw new SQLException("saveLicenceProof(): Could not establish connection to database.");

        try {
            ensureLicenceColumns(conn);

            String sql = """
                    UPDATE driverdetails
                    SET licenseNumber = ?, permitType = ?, licenseProofPath = ?, licenseVerified = FALSE
                    WHERE driverId = ?
                    """;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, licenseNumber);
                ps.setString(2, permitType);
                ps.setString(3, proofPath);
                ps.setInt(4, driverId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            log.error("saveLicenceProof(): SQL error: {}", e.getMessage());
            throw e;
        } finally {
            connector.freeConnection();
        }
    }

    @Override
    public List<Map<String, Object>> getDriversForLicenceUpload() throws SQLException {
        Connection conn = connector.getConnection();
        if (conn == null) throw new SQLException("getDriversForLicenceUpload(): Could not establish connection to database.");

        try {
            ensureLicenceColumns(conn);

            String sql = """
                    SELECT driverId, firstName, lastName, email, licenseNumber, permitType, licenseVerified
                    FROM driverdetails
                    ORDER BY firstName, lastName
                    """;

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> drivers = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> driver = new LinkedHashMap<>();
                    driver.put("driverId", rs.getInt("driverId"));
                    driver.put("name", rs.getString("firstName") + " " + rs.getString("lastName"));
                    driver.put("email", rs.getString("email"));
                    driver.put("licenseNumber", rs.getString("licenseNumber"));
                    driver.put("permitType", rs.getString("permitType"));
                    driver.put("licenseVerified", rs.getBoolean("licenseVerified"));
                    drivers.add(driver);
                }
                return drivers;
            }
        } catch (SQLException e) {
            log.error("getDriversForLicenceUpload(): SQL error: {}", e.getMessage());
            throw e;
        } finally {
            connector.freeConnection();
        }
    }

    @Override
    public List<Map<String, Object>> getLicenceProofs() throws SQLException {
        Connection conn = connector.getConnection();
        if (conn == null) throw new SQLException("getLicenceProofs(): Could not establish connection to database.");

        try {
            ensureLicenceColumns(conn);

            String sql = """
                    SELECT driverId, firstName, lastName, email, licenseNumber,
                           permitType, licenseProofPath, licenseVerified
                    FROM driverdetails
                    WHERE licenseProofPath IS NOT NULL AND licenseProofPath <> ''
                    ORDER BY licenseVerified ASC, driverId DESC
                    """;

            try (PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                List<Map<String, Object>> licences = new ArrayList<>();
                while (rs.next()) {
                    Map<String, Object> licence = new LinkedHashMap<>();
                    licence.put("driverId", rs.getInt("driverId"));
                    licence.put("name", rs.getString("firstName") + " " + rs.getString("lastName"));
                    licence.put("email", rs.getString("email"));
                    licence.put("licenseNumber", rs.getString("licenseNumber"));
                    licence.put("permitType", rs.getString("permitType"));
                    String proofPath = rs.getString("licenseProofPath");
                    licence.put("licenseProofPath", proofPath);
                    licence.put("licenseProofFileName", getFileName(proofPath));
                    licence.put("licenseVerified", rs.getBoolean("licenseVerified"));
                    licences.add(licence);
                }
                return licences;
            }
        } catch (SQLException e) {
            log.error("getLicenceProofs(): SQL error: {}", e.getMessage());
            throw e;
        } finally {
            connector.freeConnection();
        }
    }

    @Override
    public Map<String, Object> getDriverByUserId(int userId) throws SQLException {
        if (userId <= 0) {
            return null;
        }

        Connection conn = connector.getConnection();
        if (conn == null) throw new SQLException("getDriverByUserId(): Could not establish connection to database.");

        try {
            ensureLicenceColumns(conn);

            boolean hasUserIdColumn = hasColumn(conn, "driverdetails", "userId");
            String sql = hasUserIdColumn
                    ? """
                    SELECT driverId, firstName, lastName, email, licenseNumber, permitType, licenseVerified
                    FROM driverdetails
                    WHERE userId = ?
                    LIMIT 1
                    """
                    : """
                    SELECT driverId, firstName, lastName, email, licenseNumber, permitType, licenseVerified
                    FROM driverdetails
                    WHERE driverId = ?
                    LIMIT 1
                    """;

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, userId);

                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return null;
                    }

                    Map<String, Object> driver = new LinkedHashMap<>();
                    driver.put("driverId", rs.getInt("driverId"));
                    driver.put("name", rs.getString("firstName") + " " + rs.getString("lastName"));
                    driver.put("email", rs.getString("email"));
                    driver.put("licenseNumber", rs.getString("licenseNumber"));
                    driver.put("permitType", rs.getString("permitType"));
                    driver.put("licenseVerified", rs.getBoolean("licenseVerified"));
                    return driver;
                }
            }
        } catch (SQLException e) {
            log.error("getDriverByUserId(): SQL error: {}", e.getMessage());
            throw e;
        } finally {
            connector.freeConnection();
        }
    }

    @Override
    public boolean updateLicenceVerification(int driverId, boolean verified) throws SQLException {
        Connection conn = connector.getConnection();
        if (conn == null) throw new SQLException("updateLicenceVerification(): Could not establish connection to database.");

        try {
            ensureLicenceColumns(conn);

            String sql = "UPDATE driverdetails SET licenseVerified = ? WHERE driverId = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBoolean(1, verified);
                ps.setInt(2, driverId);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            log.error("updateLicenceVerification(): SQL error: {}", e.getMessage());
            throw e;
        } finally {
            connector.freeConnection();
        }
    }

    private void ensureLicenceColumns(Connection conn) throws SQLException {
        if (!hasColumn(conn, "driverdetails", "licenseProofPath")) {
            try (Statement statement = conn.createStatement()) {
                statement.executeUpdate("ALTER TABLE driverdetails ADD COLUMN licenseProofPath VARCHAR(255)");
            }
        }

        if (!hasColumn(conn, "driverdetails", "licenseVerified")) {
            try (Statement statement = conn.createStatement()) {
                statement.executeUpdate("ALTER TABLE driverdetails ADD COLUMN licenseVerified BOOLEAN NOT NULL DEFAULT FALSE");
            }
        }
    }

    private boolean hasColumn(Connection conn, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = conn.getMetaData();
        try (ResultSet rs = metaData.getColumns(conn.getCatalog(), null, tableName, columnName)) {
            return rs.next();
        }
    }

    private String getFileName(String proofPath) {
        if (proofPath == null || proofPath.isBlank()) {
            return "";
        }
        return proofPath.replace("\\", "/").substring(proofPath.replace("\\", "/").lastIndexOf('/') + 1);
    }
}
