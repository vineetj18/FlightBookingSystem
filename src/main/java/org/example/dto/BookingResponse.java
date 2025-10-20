package org.example.dto;

import org.example.enums.BookingStatus;
import org.example.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BookingResponse {
    
    private Long id;
    private String bookingId;
    private Long flightId;
    private String bookedBy;
    private String paxDetails;
    private BigDecimal price;
    private String paymentId;
    private BookingStatus status;
    private PaymentStatus paymentStatus;
    private String pnr;
    private String seatId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public BookingResponse() {}
    
    public BookingResponse(Long id, String bookingId, Long flightId, String bookedBy, 
                         String paxDetails, BigDecimal price, String paymentId, 
                         BookingStatus status, PaymentStatus paymentStatus, String pnr, 
                         String seatId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.bookingId = bookingId;
        this.flightId = flightId;
        this.bookedBy = bookedBy;
        this.paxDetails = paxDetails;
        this.price = price;
        this.paymentId = paymentId;
        this.status = status;
        this.paymentStatus = paymentStatus;
        this.pnr = pnr;
        this.seatId = seatId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getBookingId() {
        return bookingId;
    }
    
    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }
    
    public Long getFlightId() {
        return flightId;
    }
    
    public void setFlightId(Long flightId) {
        this.flightId = flightId;
    }
    
    public String getBookedBy() {
        return bookedBy;
    }
    
    public void setBookedBy(String bookedBy) {
        this.bookedBy = bookedBy;
    }
    
    public String getPaxDetails() {
        return paxDetails;
    }
    
    public void setPaxDetails(String paxDetails) {
        this.paxDetails = paxDetails;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public String getPaymentId() {
        return paymentId;
    }
    
    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }
    
    public BookingStatus getStatus() {
        return status;
    }
    
    public void setStatus(BookingStatus status) {
        this.status = status;
    }
    
    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public String getPnr() {
        return pnr;
    }
    
    public void setPnr(String pnr) {
        this.pnr = pnr;
    }
    
    public String getSeatId() {
        return seatId;
    }
    
    public void setSeatId(String seatId) {
        this.seatId = seatId;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
