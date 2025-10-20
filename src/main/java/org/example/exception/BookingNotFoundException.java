package org.example.exception;

public class BookingNotFoundException extends FlightBookingException {
    
    public BookingNotFoundException(String message) {
        super(message);
    }
    
    public BookingNotFoundException(String bookingId) {
        super("Booking not found with ID: " + bookingId);
    }
}
