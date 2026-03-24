package SpringProject.persistences;

import SpringProject.dtos.AdminReturnInspection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@Slf4j
public class AdminReturnInspectionDaoImpl implements AdminReturnInspectionDao {

    private final Connector connector;

    public AdminReturnInspectionDaoImpl(Connector connector) {
        this.connector = connector;
    }

    private static AdminReturnInspection mapRow(ResultSet rs) throws SQLException {
        return AdminReturnInspection.builder()
                .inspectionId(rs.getInt("inspectionId"))
                .bookingId(rs.getInt("bookingId"))
                .inspectedByUserId(rs.getInt("inspectedByUserId"))
                .actualReturnDate(rs.getDate("actualReturnDate").toLocalDate())
                .returnedOnTime(rs.getBoolean("returnedOnTime"))
                .damageFound(rs.getBoolean("damageFound"))
                .damageNotes(rs.getString("damageNotes"))
                .mileageIn((Integer) rs.getObject("mileageIn"))
                .fuelLevel(rs.getString("fuelLevel"))
                .build();
    }

    @Override
    public int addInspection(AdminReturnInspection inspection) throws SQLException {
        Connection conn = connector.getConnection();
        if (conn == null) throw new SQLException("addInspection(): Could not establish connection to database.");

        String sql = """
                INSERT INTO return_inspections
                (bookingId, inspectedByUserId, actualReturnDate, returnedOnTime, damageFound, damageNotes, mileageIn, fuelLevel)
                VALUES (?,?,?,?,?,?,?,?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, inspection.getBookingId());
            ps.setInt(2, inspection.getInspectedByUserId());
            ps.setDate(3, Date.valueOf(inspection.getActualReturnDate()));
            ps.setBoolean(4, inspection.isReturnedOnTime());
            ps.setBoolean(5, inspection.isDamageFound());
            ps.setString(6, inspection.getDamageNotes());
            ps.setObject(7, inspection.getMileageIn());
            ps.setString(8, inspection.getFuelLevel());

            int rows = ps.executeUpdate();
            if (rows == 0) return -1;

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
            return -1;

        } catch (SQLException e) {
            log.error("addInspection(): SQL error: {}", e.getMessage());
            throw e;
        } finally {
            connector.freeConnection();
        }
    }

    @Override
    public List<AdminReturnInspection> getByBookingId(int bookingId) throws SQLException {
        List<AdminReturnInspection> list = new ArrayList<>();

        Connection conn = connector.getConnection();
        if (conn == null) throw new SQLException("getByBookingId(): Could not establish connection to database.");

        String sql = "SELECT * FROM return_inspections WHERE bookingId = ? ORDER BY inspectionId DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookingId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
            return list;

        } catch (SQLException e) {
            log.error("getByBookingId(): SQL error: {}", e.getMessage());
            throw e;
        } finally {
            connector.freeConnection();
        }
    }
    // ... keep your existing addInspection and getByBookingId methods ...

    @Override
    public List<AdminReturnInspection> getAllInspections() throws SQLException {
        List<AdminReturnInspection> list = new ArrayList<>();
        Connection conn = connector.getConnection();
        String sql = "SELECT * FROM return_inspections ORDER BY actualReturnDate DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } finally {
            connector.freeConnection();
        }
    }

    @Override
    public AdminReturnInspection getInspectionById(int inspectionId) throws SQLException {
        Connection conn = connector.getConnection();
        String sql = "SELECT * FROM return_inspections WHERE inspectionId = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, inspectionId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
            return null;
        } finally {
            connector.freeConnection();
        }
    }

    @Override
    public void deleteInspection(int inspectionId) throws SQLException {
        Connection conn = connector.getConnection();
        String sql = "DELETE FROM return_inspections WHERE inspectionId = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, inspectionId);
            ps.executeUpdate();
        } finally {
            connector.freeConnection();
        }
    }
}