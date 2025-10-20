#!/bin/bash

# Fix Seat entity
cat > src/main/java/org/example/model/Seat.java << 'EOF'
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
EOF

# Fix Booking entity
cat > src/main/java/org/example/model/Booking.java << 'EOF'
package org.example.model;

import javax.persistence.*;
import javax.validation.constraints.*;
import org.example.enums.BookingStatus;
import org.example.enums.PaymentStatus;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "bookings")
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
    private BookingStatus status = BookingStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "pnr", unique = true)
    private String pnr;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id")
    private List<BookingSeat> assignedSeats;

    // Constructors
    public Booking() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }
    
    public Long getFlightId() { return flightId; }
    public void setFlightId(Long flightId) { this.flightId = flightId; }
    
    public String getBookedBy() { return bookedBy; }
    public void setBookedBy(String bookedBy) { this.bookedBy = bookedBy; }
    
    public String getPaxDetails() { return paxDetails; }
    public void setPaxDetails(String paxDetails) { this.paxDetails = paxDetails; }
    
    public Integer getNumberOfPassengers() { return numberOfPassengers; }
    public void setNumberOfPassengers(Integer numberOfPassengers) { this.numberOfPassengers = numberOfPassengers; }
    
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    
    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    
    public BookingStatus getStatus() { return status; }
    public void setStatus(BookingStatus status) { this.status = status; }
    
    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }
    
    public String getPnr() { return pnr; }
    public void setPnr(String pnr) { this.pnr = pnr; }
    
    public List<BookingSeat> getAssignedSeats() { return assignedSeats; }
    public void setAssignedSeats(List<BookingSeat> assignedSeats) { this.assignedSeats = assignedSeats; }

    // Business methods
    public boolean isConfirmed() {
        return status == BookingStatus.CONFIRMED;
    }

    public void confirm() {
        this.status = BookingStatus.CONFIRMED;
    }

    public void cancel() {
        this.status = BookingStatus.CANCELLED;
    }

    public void markPaymentSuccess() {
        this.paymentStatus = PaymentStatus.COMPLETED;
    }

    public void markPaymentFailed() {
        this.paymentStatus = PaymentStatus.FAILED;
    }
}
EOF

# Fix BookingSeat entity
cat > src/main/java/org/example/model/BookingSeat.java << 'EOF'
package org.example.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "booking_seats")
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
    @Column(name = "seat_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal seatPrice;

    // Constructors
    public BookingSeat() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    
    public String getSeatId() { return seatId; }
    public void setSeatId(String seatId) { this.seatId = seatId; }
    
    public String getPassengerName() { return passengerName; }
    public void setPassengerName(String passengerName) { this.passengerName = passengerName; }
    
    public BigDecimal getSeatPrice() { return seatPrice; }
    public void setSeatPrice(BigDecimal seatPrice) { this.seatPrice = seatPrice; }
}
EOF

echo "Entities fixed!"
