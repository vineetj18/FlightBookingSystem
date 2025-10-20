package org.example.exception;

public class FlightNotFoundException extends FlightBookingException {
    
    public FlightNotFoundException(String message) {
        super(message);
    }
    
    public FlightNotFoundException(Long flightId) {
        super("Flight not found with ID: " + flightId);
    }
}
