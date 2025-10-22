package org.example.util;

import org.example.dto.BookingRequest;
import org.example.dto.FlightRequest;
import org.example.dto.SearchFlightRequest;
import org.example.model.Booking;
import org.example.model.Flight;
import org.example.model.Seat;
import org.example.enums.BookingStatus;
import org.example.enums.FlightStatus;
import org.example.enums.PaymentStatus;
import org.example.enums.SeatStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TestDataBuilder {
    
    public static FlightRequest createFlightRequest() {
        FlightRequest request = new FlightRequest();
        request.setFlightNumber("FL001");
        request.setFrom("New York");
        request.setTo("Los Angeles");
        request.setFlightMetadata("Boeing 737");
        request.setDepartureTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
        request.setArrivalTime(LocalDateTime.now().plusDays(1).withHour(13).withMinute(0));
        request.setPrice(new BigDecimal("299.99"));
        request.setMaxPassengers(150);
        return request;
    }
    
    public static FlightRequest createFlightRequest(String flightNumber, String from, String to) {
        FlightRequest request = createFlightRequest();
        request.setFlightNumber(flightNumber);
        request.setFrom(from);
        request.setTo(to);
        return request;
    }
    
    public static Flight createFlight() {
        Flight flight = new Flight();
        flight.setId(1L);
        flight.setFlightNumber("FL001");
        flight.setFrom("New York");
        flight.setTo("Los Angeles");
        flight.setFlightMetadata("Boeing 737");
        flight.setDepartureTime(LocalDateTime.now().plusDays(1).withHour(10).withMinute(0));
        flight.setArrivalTime(LocalDateTime.now().plusDays(1).withHour(13).withMinute(0));
        flight.setPrice(new BigDecimal("299.99"));
        flight.setMaxPassengers(150);
        flight.setAvailableSeats(150);
        flight.setStatus(FlightStatus.SCHEDULED);
        flight.setCreatedAt(LocalDateTime.now());
        flight.setUpdatedAt(LocalDateTime.now());
        return flight;
    }
    
    public static Flight createFlight(Long id, String flightNumber, String from, String to) {
        Flight flight = createFlight();
        flight.setId(id);
        flight.setFlightNumber(flightNumber);
        flight.setFrom(from);
        flight.setTo(to);
        return flight;
    }
    
    public static BookingRequest createBookingRequest() {
        BookingRequest request = new BookingRequest();
        request.setFlightId(1L);
        request.setNumberOfPassengers(2);
        request.setPaxDetails("John Doe, Jane Doe");
        request.setBookedBy("user@example.com");
        return request;
    }
    
    public static BookingRequest createBookingRequest(Long flightId, int passengers) {
        BookingRequest request = createBookingRequest();
        request.setFlightId(flightId);
        request.setNumberOfPassengers(passengers);
        return request;
    }
    
    public static Booking createBooking() {
        Booking booking = new Booking();
        // Let JPA assign the ID
        booking.setId(null);
        booking.setBookingId(java.util.UUID.randomUUID().toString());
        booking.setFlightId(1L);
        booking.setBookedBy("user@example.com");
        booking.setPaxDetails("John Doe, Jane Doe");
        booking.setNumberOfPassengers(2);
        booking.setTotalPrice(new BigDecimal("599.98"));
        booking.setPaymentId("PAY" + System.currentTimeMillis());
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setPaymentStatus(PaymentStatus.COMPLETED);
        booking.setPnr(java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase());
        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());
        return booking;
    }
    
    public static SearchFlightRequest createSearchRequest() {
        SearchFlightRequest request = new SearchFlightRequest();
        request.setFrom("New York");
        request.setTo("Los Angeles");
        request.setPassengers(2);
        request.setDate(LocalDateTime.now().plusDays(1).withHour(0).withMinute(0));
        return request;
    }
    
    public static Seat createSeat() {
        Seat seat = new Seat();
        seat.setId(1L);
        seat.setFlightId(1L);
        seat.setSeatId("A1");
        seat.setStatus(SeatStatus.AVAILABLE);
        seat.setCreatedAt(LocalDateTime.now());
        seat.setUpdatedAt(LocalDateTime.now());
        return seat;
    }
    
    public static List<Seat> createSeatsForFlight(Long flightId, int count) {
        List<Seat> seats = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Seat seat = new Seat();
            seat.setId((long) i);
            seat.setFlightId(flightId);
            seat.setSeatId("A" + i);
            seat.setStatus(SeatStatus.AVAILABLE);
            seat.setCreatedAt(LocalDateTime.now());
            seat.setUpdatedAt(LocalDateTime.now());
            seats.add(seat);
        }
        return seats;
    }
}
