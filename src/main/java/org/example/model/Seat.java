package org.example.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.example.enums.SeatStatus;

@Entity
@Table(name = "seats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Flight ID is required")
    @Column(name = "flight_id", nullable = false)
    private Long flightId;
    
    @NotBlank(message = "Seat ID is required")
    @Column(name = "seat_id", nullable = false)
    private String seatId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private SeatStatus status = SeatStatus.AVAILABLE;
    
    // Business methods
    public boolean isAvailable() {
        return status == SeatStatus.AVAILABLE;
    }
    
    public boolean isLocked() {
        return status == SeatStatus.LOCKED;
    }
    
    public boolean isOccupied() {
        return status == SeatStatus.OCCUPIED;
    }
    
    public void lock() {
        if (isAvailable()) {
            this.status = SeatStatus.LOCKED;
        }
    }
    
    public void occupy() {
        if (isLocked()) {
            this.status = SeatStatus.OCCUPIED;
        }
    }
    
    public void release() {
        if (isLocked()) {
            this.status = SeatStatus.AVAILABLE;
        }
    }
}
