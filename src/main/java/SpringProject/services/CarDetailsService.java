package SpringProject.services;

import SpringProject.dtos.CarDetails;
import SpringProject.persistences.CarDetailsDao;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class CarDetailsService {

    private final CarDetailsDao carDetailsDao;

    public CarDetailsService(CarDetailsDao carDetailsDao) {
        this.carDetailsDao = carDetailsDao;
    }

    public List<CarDetails> getAllCars() throws Exception {
        return carDetailsDao.getAllCars();
    }

    public CarDetails getCarById(int carId) throws Exception {
        return carDetailsDao.getCarById(carId);
    }

    public CarDetails getCarByRegNumber(String regNumber) throws Exception {
        return carDetailsDao.getCarByRegNumber(regNumber);
    }

    public List<CarDetails> getAvailableCars(LocalDate pickUpDate, LocalDate returnDate) throws Exception {
        return carDetailsDao.getAvailableCars(pickUpDate, returnDate);
    }

    public int addCar(CarDetails car) throws Exception {
        return carDetailsDao.addCar(car);
    }

    public boolean updateCarStatus(int carId, String currentStatus) throws Exception {
        return carDetailsDao.updateCarStatus(carId, currentStatus);
    }
}