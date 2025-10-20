package org.example.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.example.enums.FlightStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "flights")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flight extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Flight number is required")
    @Column(name = "flight_number", nullable = false)
    private String flightNumber;
    
    @NotBlank(message = "From location is required")
    @Column(name = "from_location", nullable = false)
    private String from;
    
    @NotBlank(message = "To location is required")
    @Column(name = "to_location", nullable = false)
    private String to;
    
    @Column(name = "flight_metadata", columnDefinition = "TEXT")
    private String flightMetadata;
    
    @NotNull(message = "Departure time is required")
    @Column(name = "departure_time", nullable = false)
    private LocalDateTime departureTime;
    
    @NotNull(message = "Arrival time is required")
    @Column(name = "arrival_time", nullable = false)
    private LocalDateTime arrivalTime;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private FlightStatus status = FlightStatus.SCHEDULED;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @NotNull(message = "Max passengers is required")
    @Min(value = 1, message = "Max passengers must be at least 1")
    @Column(name = "max_passengers", nullable = false)
    private Integer maxPassengers;
    
    @NotNull(message = "Available seats is required")
    @Min(value = 0, message = "Available seats cannot be negative")
    @Column(name = "available_seats", nullable = false)
    @Builder.Default
    private Integer availableSeats = 0;
    
    // Business methods
    public boolean hasAvailableSeats() {
        return availableSeats > 0;
    }
    
    public void decrementAvailableSeats() {
        if (availableSeats > 0) {
            this.availableSeats--;
        }
    }
    
    public void incrementAvailableSeats() {
        if (availableSeats < maxPassengers) {
            this.availableSeats++;
        }
    }
}
