package SpringProject.persistences;

import SpringProject.dtos.CarDetails;
import java.time.LocalDate;
import java.util.List;

public interface CarDetailsDao {

    List<CarDetails>getAllCars();

    CarDetails getCarById(int carID);

    CarDetails getCarByRegNumber(String regNumber);


    List<CarDetails> getAvailableCars(LocalDate pickUpDate, LocalDate returnDate);

    int addCar(CarDetails car);

    boolean updateCarStatus(int carID, String status);
}


