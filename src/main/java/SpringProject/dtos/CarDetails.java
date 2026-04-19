package SpringProject.dtos;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CarDetails {
    private int carId;
    private String regNumber;
    private String make;
    private String model;
    private int carYear;

    private String colour;
    private Integer mileage;

    private String transmission;   // manual,  automatic
    private String currentStatus;
    private String fuelType;
    private Double dailyRate;
    private String imageUrl;
}
