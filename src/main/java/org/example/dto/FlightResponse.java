package org.example.dto;

import lombok.*;
import org.example.enums.FlightStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightResponse {
    
    private Long id;
    private String flightNumber;
    private String from;
    private String to;
    private String flightMetadata;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private FlightStatus status;
    private BigDecimal price;
    private Integer maxPassengers;
    private Integer availableSeats;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
