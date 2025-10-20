package org.example.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightRequest {
    
    @NotBlank(message = "Flight number is required")
    private String flightNumber;
    
    @NotBlank(message = "From location is required")
    private String from;
    
    @NotBlank(message = "To location is required")
    private String to;
    
    private String flightMetadata;
    
    @NotNull(message = "Departure time is required")
    @Future(message = "Departure time must be in the future")
    private LocalDateTime departureTime;
    
    @NotNull(message = "Arrival time is required")
    @Future(message = "Arrival time must be in the future")
    private LocalDateTime arrivalTime;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;
    
    @NotNull(message = "Max passengers is required")
    @Min(value = 1, message = "Max passengers must be at least 1")
    private Integer maxPassengers;
}
