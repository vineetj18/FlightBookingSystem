package org.example.exception;

public class SeatNotAvailableException extends FlightBookingException {
    
    public SeatNotAvailableException(String message) {
        super(message);
    }
    
    public SeatNotAvailableException(String seatId, Long flightId) {
        super("Seat " + seatId + " is not available for flight " + flightId);
    }
}
