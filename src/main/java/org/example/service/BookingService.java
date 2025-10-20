package org.example.service;

import org.example.dto.BookingRequest;
import org.example.dto.BookingResponse;
import org.example.exception.BookingNotFoundException;
import org.example.exception.FlightNotFoundException;
import org.example.exception.PaymentFailedException;
import org.example.exception.SeatNotAvailableException;
import org.example.model.Booking;
import org.example.model.BookingSeat;
import org.example.model.Flight;
import org.example.model.Seat;
import org.example.repository.BookingRepository;
import org.example.repository.FlightRepository;
import org.example.enums.BookingStatus;
import org.example.enums.PaymentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class BookingService {
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private FlightRepository flightRepository;
    
    @Autowired
    private SeatService seatService;
    
    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private RedisLockService redisLockService;
    
    public BookingResponse createBooking(BookingRequest request) {
        // Validate flight exists
        Flight flight = flightRepository.findById(request.getFlightId())
                .orElseThrow(() -> new FlightNotFoundException(request.getFlightId()));
        
        // Check if flight has enough available seats
        if (flight.getAvailableSeats() < request.getNumberOfPassengers()) {
            throw new SeatNotAvailableException("Not enough seats available for flight " + request.getFlightId());
        }
        
        // Get available seats sequentially
        List<Seat> availableSeats = seatService.getAvailableSeatsSequentially(
            request.getFlightId(), request.getNumberOfPassengers());
        
        if (availableSeats.size() < request.getNumberOfPassengers()) {
            throw new SeatNotAvailableException("Not enough seats available for flight " + request.getFlightId());
        }
        
        // Acquire Redis locks for all seats
        List<String> lockValues = new ArrayList<>();
        try {
            for (Seat seat : availableSeats) {
                String lockValue = redisLockService.acquireSeatLock(request.getFlightId(), seat.getSeatId());
                if (lockValue == null) {
                    // Release all previously acquired locks
                    releaseAllLocks(request.getFlightId(), availableSeats, lockValues);
                    throw new SeatNotAvailableException("Seat " + seat.getSeatId() + " is currently being booked by another user");
                }
                lockValues.add(lockValue);
            }
            
            // Create booking
            String bookingId = generateBookingId();
            BigDecimal totalPrice = flight.getPrice().multiply(BigDecimal.valueOf(request.getNumberOfPassengers()));
            
            Booking booking = Booking.builder()
                    .bookingId(bookingId)
                    .flightId(request.getFlightId())
                    .bookedBy(request.getBookedBy())
                    .paxDetails(request.getPaxDetails())
                    .numberOfPassengers(request.getNumberOfPassengers())
                    .totalPrice(totalPrice)
                    .build();
            
            // Save booking
            Booking savedBooking = bookingRepository.save(booking);
            
            // Process payment
            PaymentService.PaymentResult paymentResult = paymentService.processPayment(
                bookingId, totalPrice, "CREDIT_CARD");
            
            if (paymentResult.isSuccess()) {
                // Update booking with payment details
                savedBooking.setPaymentId(paymentResult.getTransactionId());
                savedBooking.markPaymentSuccess();
                savedBooking.confirm();
                savedBooking.setPnr(generatePNR());
                
                // Create BookingSeat entries and lock seats
                List<BookingSeat> bookingSeats = new ArrayList<>();
                String[] passengerNames = request.getPaxDetails().split(",");
                
                for (int i = 0; i < availableSeats.size(); i++) {
                    Seat seat = availableSeats.get(i);
                    String passengerName = (i < passengerNames.length) ? passengerNames[i].trim() : "Passenger " + (i + 1);
                    
                    BookingSeat bookingSeat = BookingSeat.builder()
                            .bookingId(savedBooking.getId())
                            .seatId(seat.getSeatId())
                            .passengerName(passengerName)
                            .seatPrice(flight.getPrice())
                            .build();
                    
                    bookingSeats.add(bookingSeat);
                    
                    // Lock the seat
                    seatService.lockSeat(request.getFlightId(), seat.getSeatId());
                }
                
                savedBooking.setAssignedSeats(bookingSeats);
                
                // Decrement available seats
                flight.setAvailableSeats(flight.getAvailableSeats() - request.getNumberOfPassengers());
                flightRepository.save(flight);
                
            } else {
                // Payment failed
                savedBooking.markPaymentFailed();
                savedBooking.cancel();
                throw new PaymentFailedException("Payment failed: " + paymentResult.getMessage());
            }
            
            // Save updated booking
            Booking finalBooking = bookingRepository.save(savedBooking);
            
            return convertToResponse(finalBooking);
            
        } finally {
            // Release all locks
            releaseAllLocks(request.getFlightId(), availableSeats, lockValues);
        }
    }
    
    public BookingResponse getBookingById(String bookingId) {
        Booking booking = bookingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
        return convertToResponse(booking);
    }
    
    public BookingResponse getBookingByPnr(String pnr) {
        Booking booking = bookingRepository.findByPnr(pnr)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with PNR: " + pnr));
        return convertToResponse(booking);
    }
    
    public List<BookingResponse> getBookingsByUser(String bookedBy) {
        List<Booking> bookings = bookingRepository.findByBookedBy(bookedBy);
        return bookings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public BookingResponse cancelBooking(String bookingId) {
        Booking booking = bookingRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new BookingNotFoundException(bookingId));
        
        if (booking.isCancelled()) {
            throw new IllegalArgumentException("Booking is already cancelled");
        }
        
        if (!booking.isPending() && !booking.isConfirmed()) {
            throw new IllegalArgumentException("Cannot cancel booking in current status: " + booking.getStatus());
        }
        
        // Cancel booking
        booking.cancel();
        
        // Release seat if it was locked
        if (booking.isConfirmed()) {
            seatService.releaseSeat(booking.getFlightId(), booking.getSeatId());
            
            // Increment available seats
            Flight flight = flightRepository.findById(booking.getFlightId())
                    .orElseThrow(() -> new FlightNotFoundException(booking.getFlightId()));
            flight.incrementAvailableSeats();
            flightRepository.save(flight);
        }
        
        // Process refund if payment was successful
        if (booking.isPaymentSuccessful() && booking.getPaymentId() != null) {
            PaymentService.PaymentResult refundResult = paymentService.refundPayment(
                booking.getPaymentId(), booking.getPrice());
            
            if (refundResult.isSuccess()) {
                booking.setPaymentStatus(PaymentStatus.REFUNDED);
            }
        }
        
        Booking updatedBooking = bookingRepository.save(booking);
        return convertToResponse(updatedBooking);
    }
    
    public List<BookingResponse> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return bookings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    private String generateBookingId() {
        return "BK" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private String generatePNR() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }
    
    private void releaseAllLocks(Long flightId, List<Seat> seats, List<String> lockValues) {
        for (int i = 0; i < seats.size() && i < lockValues.size(); i++) {
            redisLockService.releaseSeatLock(flightId, seats.get(i).getSeatId(), lockValues.get(i));
        }
    }
    
    private BookingResponse convertToResponse(Booking booking) {
        return new BookingResponse(
            booking.getId(),
            booking.getBookingId(),
            booking.getFlightId(),
            booking.getBookedBy(),
            booking.getPaxDetails(),
            booking.getPrice(),
            booking.getPaymentId(),
            booking.getStatus(),
            booking.getPaymentStatus(),
            booking.getPnr(),
            booking.getSeatId(),
            booking.getCreatedAt(),
            booking.getUpdatedAt()
        );
    }
}
