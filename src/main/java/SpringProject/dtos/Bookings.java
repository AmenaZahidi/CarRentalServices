package SpringProject.dtos;
/*CREATE TABLE bookings (
        bookingId INT AUTO_INCREMENT PRIMARY KEY,
        driverId INT NOT NULL,
        userId INT NOT NULL,
        carId INT NOT NULL,
        pickupDatetime DATETIME NOT NULL,
        returnDatetime DATETIME NOT NULL,
        pickupLocationId INT,
        dropOffLocationId INT,
        totalPrice DECIMAL(10, 2),
status ENUM('confirmed', 'active', 'returned', 'cancelled') DEFAULT 'confirmed',

FOREIGN KEY (userId) REFERENCES users(userId),
FOREIGN KEY (carId) REFERENCES carDetails(carId),
FOREIGN KEY (driverId) REFERENCES driverdetails(driverId),
FOREIGN KEY (pickupLocationId) REFERENCES location(locationId),
FOREIGN KEY (dropOffLocationId) REFERENCES location(locationId)
        );*/
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Bookings {
    @PositiveOrZero(message = "Booking id cannot be negative")
    private Integer bookingId;

    private Integer driverId;

    private Integer userId;

    @NotNull(message = "Car is required")
    @Positive(message = "Car is required")
    private Integer carId;

    @NotNull(message = "Pickup location is required")
    @Positive(message = "Pickup location is required")
    private Integer pickupLocationId;

    @NotNull(message = "Drop-off location is required")
    @Positive(message = "Drop-off location is required")
    private Integer dropOffLocationId;

    @NotNull(message = "Pickup date and time is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private Date pickupDateTime;

    @NotNull(message = "Return date and time is required")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private Date returnDateTime;

    @DecimalMin(value = "0.0", message = "Total price cannot be negative")
    private Double totalPrice;

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "confirmed|active|returned|cancelled",
            message = "Status must be confirmed, active, returned, or cancelled")
    private String status = "confirmed";
}
