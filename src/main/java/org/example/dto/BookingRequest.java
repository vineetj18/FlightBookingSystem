package org.example.dto;

import javax.validation.constraints.*;

public class BookingRequest {

    @NotNull(message = "Flight ID is required")
    private Long flightId;

    @NotNull(message = "Number of passengers is required")
    @Min(value = 1, message = "Number of passengers must be at least 1")
    @Max(value = 9, message = "Number of passengers cannot exceed 9")
    private Integer numberOfPassengers;

    @NotBlank(message = "Passenger details are required")
    private String paxDetails;

    @NotBlank(message = "Booked by is required")
    private String bookedBy;

    // Constructors
    public BookingRequest() {}

    // Getters and Setters
    public Long getFlightId() { return flightId; }
    public void setFlightId(Long flightId) { this.flightId = flightId; }
    
    public Integer getNumberOfPassengers() { return numberOfPassengers; }
    public void setNumberOfPassengers(Integer numberOfPassengers) { this.numberOfPassengers = numberOfPassengers; }
    
    public String getPaxDetails() { return paxDetails; }
    public void setPaxDetails(String paxDetails) { this.paxDetails = paxDetails; }
    
    public String getBookedBy() { return bookedBy; }
    public void setBookedBy(String bookedBy) { this.bookedBy = bookedBy; }
}
