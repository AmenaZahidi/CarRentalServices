package SpringProject.persistences;

import SpringProject.dtos.AdminReturnInspection;
import java.sql.SQLException;
import java.util.List;

public interface AdminReturnInspectionDao {
    int addInspection(AdminReturnInspection inspection) throws SQLException;
    List<AdminReturnInspection> getByBookingId(int bookingId) throws SQLException;

    List<AdminReturnInspection> getAllInspections() throws SQLException;
    AdminReturnInspection getInspectionById(int inspectionId) throws SQLException;
    void deleteInspection(int inspectionId) throws SQLException;
}