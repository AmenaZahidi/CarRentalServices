package SpringProject.persistences;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface DriverLicenceDao {
    boolean saveLicenceProof(int driverId, String licenseNumber, String permitType, String proofPath) throws SQLException;
    List<Map<String, Object>> getDriversForLicenceUpload() throws SQLException;
    List<Map<String, Object>> getLicenceProofs() throws SQLException;
    boolean updateLicenceVerification(int driverId, boolean verified) throws SQLException;
}
