package SpringProject.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    private Integer paymentId;
    private Integer bookingId;
    private Date paymentDate;
    private Double amount;
    private String paymentStatus;
    private String transactionRef;
}
