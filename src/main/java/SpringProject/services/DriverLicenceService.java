package SpringProject.services;

import SpringProject.persistences.DriverLicenceDao;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class DriverLicenceService {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "pdf", "webp", "jfif", "heic");

    private final DriverLicenceDao driverLicenceDao;

    public DriverLicenceService(DriverLicenceDao driverLicenceDao) {
        this.driverLicenceDao = driverLicenceDao;
    }

    public void saveLicenceProof(int driverId,
                                 String licenseNumber,
                                 String permitType,
                                 MultipartFile licenceProof) throws SQLException, IOException {
        validateInput(driverId, licenseNumber, permitType, licenceProof);

        String cleanLicenseNumber = normaliseLicenceNumber(licenseNumber);
        String proofPath = saveFile(driverId, licenceProof);
        boolean saved = driverLicenceDao.saveLicenceProof(driverId, cleanLicenseNumber, permitType, proofPath);
        if (!saved) {
            throw new IllegalArgumentException("Driver ID does not exist.");
        }
    }

    public List<Map<String, Object>> getDriversForLicenceUpload() throws SQLException {
        return driverLicenceDao.getDriversForLicenceUpload();
    }

    public List<Map<String, Object>> getLicenceProofs() throws SQLException {
        return driverLicenceDao.getLicenceProofs();
    }

    public Map<String, Object> getDriverByUserId(int userId) throws SQLException {
        return driverLicenceDao.getDriverByUserId(userId);
    }

    public boolean approveLicence(int driverId) throws SQLException {
        if (driverId <= 0) {
            throw new IllegalArgumentException("Invalid driver ID.");
        }
        return driverLicenceDao.updateLicenceVerification(driverId, true);
    }

    public boolean rejectLicence(int driverId) throws SQLException {
        if (driverId <= 0) {
            throw new IllegalArgumentException("Invalid driver ID.");
        }
        return driverLicenceDao.updateLicenceVerification(driverId, false);
    }

    public Path getLicenceProofFile(String fileName) throws IOException {
        if (fileName == null || fileName.isBlank() || fileName.contains("..")) {
            throw new IOException("Invalid licence proof file.");
        }

        Path uploadDirectory = Path.of("uploads", "licences").toAbsolutePath().normalize();
        Path proofFile = uploadDirectory.resolve(fileName).normalize();

        if (!proofFile.startsWith(uploadDirectory) || !Files.exists(proofFile) || !Files.isRegularFile(proofFile)) {
            throw new IOException("Licence proof file was not found.");
        }

        return proofFile;
    }

    private void validateInput(int driverId,
                               String licenseNumber,
                               String permitType,
                               MultipartFile licenceProof) {
        if (driverId <= 0) {
            throw new IllegalArgumentException("Enter a valid Driver ID.");
        }
        if (normaliseLicenceNumber(licenseNumber).length() < 4) {
            throw new IllegalArgumentException("Enter a valid licence number.");
        }
        if (!"manual".equals(permitType) && !"automatic".equals(permitType)) {
            throw new IllegalArgumentException("Choose manual or automatic licence type.");
        }
        if (licenceProof == null || licenceProof.isEmpty()) {
            throw new IllegalArgumentException("Upload a licence proof file.");
        }

        String extension = getExtension(licenceProof.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Licence proof must be JPG, JPEG, PNG, WEBP, JFIF, HEIC, or PDF.");
        }
    }

    private String saveFile(int driverId, MultipartFile licenceProof) throws IOException {
        Path uploadDirectory = Path.of("uploads", "licences").toAbsolutePath().normalize();
        Files.createDirectories(uploadDirectory);

        String extension = getExtension(licenceProof.getOriginalFilename());
        String fileName = "driver-" + driverId + "-" + UUID.randomUUID() + "." + extension;
        Path targetFile = uploadDirectory.resolve(fileName).normalize();

        if (!targetFile.startsWith(uploadDirectory)) {
            throw new IOException("Invalid upload path.");
        }

        Files.copy(licenceProof.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
        return Path.of("uploads", "licences", fileName).toString();
    }

    private String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    private String normaliseLicenceNumber(String licenseNumber) {
        return licenseNumber == null ? "" : licenseNumber.replaceAll("\\s+", "").toUpperCase();
    }
}
