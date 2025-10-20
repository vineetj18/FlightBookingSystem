package org.example.dto;

import javax.validation.constraints.*;
import java.time.LocalDateTime;

public class SearchFlightRequest {
    
    @NotBlank(message = "From location is required")
    private String from;
    
    @NotBlank(message = "To location is required")
    private String to;
    
    @NotNull(message = "Passengers count is required")
    @Min(value = 1, message = "Passengers count must be at least 1")
    @Max(value = 9, message = "Passengers count cannot exceed 9")
    private Integer passengers;
    
    @NotNull(message = "Date is required")
    @Future(message = "Date must be in the future")
    private LocalDateTime date;
    
    // Constructors
    public SearchFlightRequest() {}
    
    public SearchFlightRequest(String from, String to, Integer passengers, LocalDateTime date) {
        this.from = from;
        this.to = to;
        this.passengers = passengers;
        this.date = date;
    }
    
    // Getters and Setters
    public String getFrom() {
        return from;
    }
    
    public void setFrom(String from) {
        this.from = from;
    }
    
    public String getTo() {
        return to;
    }
    
    public void setTo(String to) {
        this.to = to;
    }
    
    public Integer getPassengers() {
        return passengers;
    }
    
    public void setPassengers(Integer passengers) {
        this.passengers = passengers;
    }
    
    public LocalDateTime getDate() {
        return date;
    }
    
    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
