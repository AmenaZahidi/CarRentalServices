package SpringProject.dtos;
/*CREATE TABLE bookings (
        bookingId INT AUTO_INCREMENT PRIMARY KEY,
        driverId INT NOT NULL,
        userId INT NOT NULL,
        carId INT NOT NULL,
        pickupDatetime DATETIME NOT NULL,
        returnDatetime DATETIME NOT NULL,
        pickupLocationId INT,
        totalPrice DECIMAL(10, 2),
status ENUM('confirmed', 'active', 'returned', 'cancelled') DEFAULT 'confirmed',

FOREIGN KEY (userId) REFERENCES users(userId),
FOREIGN KEY (carId) REFERENCES carDetails(carId),
FOREIGN KEY (driverId) REFERENCES driverdetails(driverId),
FOREIGN KEY (pickupLocationId) REFERENCES location(locationId)
        );*/
import lombok.*;

import java.util.Date;

@Data
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Bookings {
    @NonNull
    private int bookingId;
    @NonNull
    private int driverId;
    @NonNull
    private int userId;
    @NonNull
    private int carId;
    @NonNull
    private int pickupLocationId;
    @NonNull
    private Date pickupDateTime;
    @NonNull
    private Date returnDateTime;
@NonNull
    private double totalPrice;
    @NonNull
    private String status;
}
