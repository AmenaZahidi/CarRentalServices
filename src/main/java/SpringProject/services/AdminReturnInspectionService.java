package SpringProject.services;

import SpringProject.dtos.AdminReturnInspection;
import SpringProject.persistences.AdminReturnInspectionDao;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AdminReturnInspectionService {
    private final AdminReturnInspectionDao dao;

    public AdminReturnInspectionService(AdminReturnInspectionDao dao) {
        this.dao = dao;
    }

    public int addInspection(AdminReturnInspection inspection) throws Exception {
        return dao.addInspection(inspection);
    }

    // ADD THESE:
    public List<AdminReturnInspection> getAllInspections() throws Exception {
        return dao.getAllInspections();
    }

    public AdminReturnInspection getInspectionById(int id) throws Exception {
        return dao.getInspectionById(id);
    }

    public void deleteInspection(int id) throws Exception {
        dao.deleteInspection(id);
    }

    public List<AdminReturnInspection> getByBookingId(int bookingId) throws Exception {
        return dao.getByBookingId(bookingId);
    }
}