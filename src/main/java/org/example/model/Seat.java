package org.example.model;

import javax.persistence.*;
import javax.validation.constraints.*;
import org.example.enums.SeatStatus;

@Entity
@Table(name = "seats")
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
    private SeatStatus status = SeatStatus.AVAILABLE;

    // Constructors
    public Seat() {}
    
    public Seat(Long flightId, String seatId) {
        this.flightId = flightId;
        this.seatId = seatId;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getFlightId() { return flightId; }
    public void setFlightId(Long flightId) { this.flightId = flightId; }
    
    public String getSeatId() { return seatId; }
    public void setSeatId(String seatId) { this.seatId = seatId; }
    
    public SeatStatus getStatus() { return status; }
    public void setStatus(SeatStatus status) { this.status = status; }

    // Business methods
    public boolean isAvailable() {
        return status == SeatStatus.AVAILABLE;
    }

    public void lock() {
        this.status = SeatStatus.LOCKED;
    }

    public void occupy() {
        this.status = SeatStatus.OCCUPIED;
    }

    public void release() {
        this.status = SeatStatus.AVAILABLE;
    }
}
