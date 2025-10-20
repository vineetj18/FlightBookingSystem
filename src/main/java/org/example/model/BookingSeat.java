package org.example.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "booking_seats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingSeat extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull(message = "Booking ID is required")
    @Column(name = "booking_id", nullable = false)
    private Long bookingId;
    
    @NotBlank(message = "Seat ID is required")
    @Column(name = "seat_id", nullable = false)
    private String seatId;
    
    @NotBlank(message = "Passenger name is required")
    @Column(name = "passenger_name", nullable = false)
    private String passengerName;
    
    @NotNull(message = "Seat price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Seat price must be greater than 0")
    @Column(name = "seat_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal seatPrice;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", insertable = false, updatable = false)
    private Booking booking;
}
