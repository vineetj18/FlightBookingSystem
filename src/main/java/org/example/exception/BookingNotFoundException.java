package org.example.exception;

public class BookingNotFoundException extends FlightBookingException {
    
    public BookingNotFoundException(String message) {
        super(message);
    }
}
