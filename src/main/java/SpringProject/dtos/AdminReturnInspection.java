package SpringProject.dtos;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminReturnInspection {
    private Integer inspectionId;
    private Integer bookingId;
    private Integer inspectedByUserId;

    private LocalDate actualReturnDate;
    private boolean returnedOnTime;

    private boolean damageFound;
    private String damageNotes;

    private Integer mileageIn;
    private String fuelLevel;
}