package org.example.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequest {
    
    @NotNull(message = "Flight ID is required")
    private Long flightId;
    
    @NotNull(message = "Number of passengers is required")
    @Min(value = 1, message = "Number of passengers must be at least 1")
    @Max(value = 9, message = "Number of passengers cannot exceed 9")
    private Integer numberOfPassengers;
    
    @NotBlank(message = "Passenger details are required")
    private String paxDetails;
    
    @NotBlank(message = "Booked by is required")
    private String bookedBy;
}
