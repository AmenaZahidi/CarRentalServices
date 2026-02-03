package SpringProject.dtos;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CarDetails {
    private int carID;
    private String regNumber;
    private String make;
    private String model;
    private int year;

    private String colour;
    private Integer mileage;
    private String fuelType;

    private double dailyRate;
    private String status;
}
