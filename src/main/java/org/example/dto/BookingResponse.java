package org.example.dto;

import org.example.enums.BookingStatus;
import org.example.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class BookingResponse {
    
    private Long id;
    private String bookingId;
    private Long flightId;
    private String bookedBy;
    private String paxDetails;
    private Integer numberOfPassengers;
    private BigDecimal totalPrice;
    private String paymentId;
    private BookingStatus status;
    private PaymentStatus paymentStatus;
    private String pnr;
    private List<String> assignedSeats;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public BookingResponse() {}

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
    
    public List<String> getAssignedSeats() { return assignedSeats; }
    public void setAssignedSeats(List<String> assignedSeats) { this.assignedSeats = assignedSeats; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
