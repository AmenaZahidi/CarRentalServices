package SpringProject.persistences;

import SpringProject.dtos.CarDetails;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import SpringProject.dtos.CarDetails;


public interface CarDetailsDao {

    List<CarDetails>getAllCars() throws SQLException;

    CarDetails getCarById(int carID) throws SQLException;

    CarDetails getCarByRegNumber(String regNumber) throws SQLException;


    List<CarDetails> getAvailableCars(LocalDate pickUpDate, LocalDate returnDate) throws SQLException;

    int addCar(CarDetails car) throws SQLException;

    boolean updateCarStatus(int carID, String currentStatus) throws SQLException;
}


