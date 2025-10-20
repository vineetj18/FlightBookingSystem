package org.example.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.example.enums.BookingStatus;
import org.example.enums.PaymentStatus;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Booking ID is required")
    @Column(name = "booking_id", unique = true, nullable = false)
    private String bookingId;
    
    @NotNull(message = "Flight ID is required")
    @Column(name = "flight_id", nullable = false)
    private Long flightId;
    
    @NotBlank(message = "Booked by is required")
    @Column(name = "booked_by", nullable = false)
    private String bookedBy;
    
    @Column(name = "pax_details", columnDefinition = "TEXT")
    private String paxDetails;
    
    @NotNull(message = "Number of passengers is required")
    @Min(value = 1, message = "Number of passengers must be at least 1")
    @Column(name = "number_of_passengers", nullable = false)
    private Integer numberOfPassengers;
    
    @NotNull(message = "Total price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Total price must be greater than 0")
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;
    
    @Column(name = "payment_id")
    private String paymentId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;
    
    @Column(name = "pnr", unique = true)
    private String pnr;
    
    @OneToMany(mappedBy = "bookingId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookingSeat> assignedSeats;
    
    // Business methods
    public boolean isConfirmed() {
        return status == BookingStatus.CONFIRMED;
    }
    
    public boolean isPending() {
        return status == BookingStatus.PENDING;
    }
    
    public boolean isCancelled() {
        return status == BookingStatus.CANCELLED;
    }
    
    public boolean isPaymentSuccessful() {
        return paymentStatus == PaymentStatus.SUCCESS;
    }
    
    public void confirm() {
        if (isPending()) {
            this.status = BookingStatus.CONFIRMED;
        }
    }
    
    public void cancel() {
        if (isPending() || isConfirmed()) {
            this.status = BookingStatus.CANCELLED;
        }
    }
    
    public void markPaymentSuccess() {
        this.paymentStatus = PaymentStatus.SUCCESS;
    }
    
    public void markPaymentFailed() {
        this.paymentStatus = PaymentStatus.FAILED;
    }
}
