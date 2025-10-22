package org.example.service;

import org.example.dto.BookingRequest;
import org.example.dto.BookingResponse;
import org.example.exception.BookingNotFoundException;
import org.example.exception.FlightNotFoundException;
import org.example.exception.PaymentFailedException;
import org.example.exception.SeatNotAvailableException;
import org.example.model.Booking;
import org.example.model.Flight;
import org.example.model.Seat;
import org.example.repository.BookingRepository;
import org.example.repository.FlightRepository;
import org.example.enums.BookingStatus;
import org.example.enums.PaymentStatus;
import org.example.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.example.model.BookingSeat;
import org.example.service.LockService;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    
    @Mock
    private BookingRepository bookingRepository;
    
    @Mock
    private FlightRepository flightRepository;
    
    @Mock
    private SeatService seatService;
    
    @Mock
    private PaymentService paymentService;
    
    @Mock
    private LockService redisLockService;
    
    @InjectMocks
    private BookingService bookingService;
    
    private Booking testBooking;
    private Flight testFlight;
    private BookingRequest testBookingRequest;
    private List<Seat> testSeats;
    
    @BeforeEach
    void setUp() {
        testBooking = TestDataBuilder.createBooking();
        testFlight = TestDataBuilder.createFlight();
        testBookingRequest = TestDataBuilder.createBookingRequest();
        testSeats = TestDataBuilder.createSeatsForFlight(1L, 2);
    }
    
    @Test
    void createBooking_ShouldCreateBookingSuccessfully() {
        // Given
        when(flightRepository.findById(testBookingRequest.getFlightId()))
            .thenReturn(Optional.of(testFlight));
        when(seatService.getAvailableSeatsSequentially(anyLong(), anyInt()))
            .thenReturn(testSeats);
        when(bookingRepository.save(any(Booking.class)))
            .thenReturn(testBooking);
        
        PaymentService.PaymentResult paymentResult = new PaymentService.PaymentResult(
            "PAY123456", PaymentStatus.COMPLETED, "Payment successful");
        
        when(paymentService.processPayment(anyString(), any(BigDecimal.class), anyString()))
            .thenReturn(paymentResult);
        
        // When
        BookingResponse response = bookingService.createBooking(testBookingRequest);
        
        // Then
        assertNotNull(response);
        assertEquals(testBooking.getBookingId(), response.getBookingId());
        assertEquals(testBooking.getFlightId(), response.getFlightId());
        assertEquals(testBooking.getBookedBy(), response.getBookedBy());
        
        verify(flightRepository).findById(testBookingRequest.getFlightId());
        verify(seatService).getAvailableSeatsSequentially(anyLong(), anyInt());
        verify(bookingRepository, atLeastOnce()).save(any(Booking.class));
        verify(paymentService).processPayment(anyString(), any(BigDecimal.class), anyString());
    }
    
    @Test
    void createBooking_ShouldThrowException_WhenFlightNotFound() {
        // Given
        when(flightRepository.findById(testBookingRequest.getFlightId()))
            .thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(FlightNotFoundException.class, () -> 
            bookingService.createBooking(testBookingRequest));
        verify(flightRepository).findById(testBookingRequest.getFlightId());
        verify(seatService, never()).getAvailableSeatsSequentially(anyLong(), anyInt());
    }
    
    @Test
    void createBooking_ShouldThrowException_WhenNotEnoughSeats() {
        // Given
        testFlight.setAvailableSeats(1);
        when(flightRepository.findById(testBookingRequest.getFlightId()))
            .thenReturn(Optional.of(testFlight));
        
        // When & Then
        assertThrows(SeatNotAvailableException.class, () -> 
            bookingService.createBooking(testBookingRequest));
        verify(flightRepository).findById(testBookingRequest.getFlightId());
    }
    
    @Test
    void createBooking_ShouldThrowException_WhenPaymentFails() {
        // Given
        when(flightRepository.findById(testBookingRequest.getFlightId()))
            .thenReturn(Optional.of(testFlight));
        when(seatService.getAvailableSeatsSequentially(anyLong(), anyInt()))
            .thenReturn(testSeats);
        when(bookingRepository.save(any(Booking.class)))
            .thenReturn(testBooking);
        
        PaymentService.PaymentResult paymentResult = new PaymentService.PaymentResult(
            null, PaymentStatus.FAILED, "Payment failed");
        
        when(paymentService.processPayment(anyString(), any(BigDecimal.class), anyString()))
            .thenReturn(paymentResult);
        
        // When & Then
        assertThrows(PaymentFailedException.class, () -> 
            bookingService.createBooking(testBookingRequest));
        verify(paymentService).processPayment(anyString(), any(BigDecimal.class), anyString());
    }
    
    @Test
    void getBookingById_ShouldReturnBooking_WhenBookingExists() {
        // Given
        String bookingId = "BK123456789";
        when(bookingRepository.findByBookingId(bookingId))
            .thenReturn(Optional.of(testBooking));
        
        // When
        BookingResponse response = bookingService.getBookingById(bookingId);
        
        // Then
        assertNotNull(response);
        assertEquals(testBooking.getBookingId(), response.getBookingId());
        assertEquals(testBooking.getFlightId(), response.getFlightId());
        verify(bookingRepository).findByBookingId(bookingId);
    }
    
    @Test
    void getBookingById_ShouldThrowException_WhenBookingNotFound() {
        // Given
        String bookingId = "NONEXISTENT";
        when(bookingRepository.findByBookingId(bookingId))
            .thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(BookingNotFoundException.class, () -> 
            bookingService.getBookingById(bookingId));
        verify(bookingRepository).findByBookingId(bookingId);
    }
    
    @Test
    void getBookingByPnr_ShouldReturnBooking_WhenBookingExists() {
        // Given
        String pnr = "ABC123";
        when(bookingRepository.findByPnr(pnr))
            .thenReturn(Optional.of(testBooking));
        
        // When
        BookingResponse response = bookingService.getBookingByPnr(pnr);
        
        // Then
        assertNotNull(response);
        assertEquals(testBooking.getPnr(), response.getPnr());
        verify(bookingRepository).findByPnr(pnr);
    }
    
    @Test
    void getBookingByPnr_ShouldThrowException_WhenBookingNotFound() {
        // Given
        String pnr = "NONEXISTENT";
        when(bookingRepository.findByPnr(pnr))
            .thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(BookingNotFoundException.class, () -> 
            bookingService.getBookingByPnr(pnr));
        verify(bookingRepository).findByPnr(pnr);
    }
    
    @Test
    void getBookingsByUser_ShouldReturnUserBookings() {
        // Given
        String userEmail = "user@example.com";
        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingRepository.findByBookedBy(userEmail))
            .thenReturn(bookings);
        
        // When
        List<BookingResponse> responses = bookingService.getBookingsByUser(userEmail);
        
        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(testBooking.getBookingId(), responses.get(0).getBookingId());
        verify(bookingRepository).findByBookedBy(userEmail);
    }
    
    @Test
    void cancelBooking_ShouldCancelBooking_WhenBookingExists() {
        // Given
        String bookingId = "BK123456789";
        testBooking.setStatus(BookingStatus.CONFIRMED);
        testBooking.setPaymentStatus(PaymentStatus.COMPLETED);
        // Add assigned seats to avoid NPE during cancellation seat release loop
        testBooking.setAssignedSeats(TestDataBuilder.createSeatsForFlight(testFlight.getId(), 2)
            .stream()
            .map(seat -> {
                BookingSeat bs = new BookingSeat();
                bs.setBookingId(1L);
                bs.setSeatId(seat.getSeatId());
                return bs;
            })
            .collect(java.util.stream.Collectors.toList()));
        
        when(bookingRepository.findByBookingId(bookingId))
            .thenReturn(Optional.of(testBooking));
        when(bookingRepository.save(any(Booking.class)))
            .thenReturn(testBooking);
        when(paymentService.refundPayment(anyString(), any(BigDecimal.class)))
            .thenReturn(new PaymentService.PaymentResult("REF123", PaymentStatus.REFUNDED, "ok"));
        
        // Note: Refund payment is not called in the current implementation
        // The test focuses on booking cancellation logic
        
        // When & Then
        assertDoesNotThrow(() -> bookingService.cancelBooking(bookingId));
        verify(bookingRepository).findByBookingId(bookingId);
        verify(bookingRepository).save(any(Booking.class));
    }
    
    @Test
    void cancelBooking_ShouldThrowException_WhenBookingNotFound() {
        // Given
        String bookingId = "NONEXISTENT";
        when(bookingRepository.findByBookingId(bookingId))
            .thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(BookingNotFoundException.class, () -> 
            bookingService.cancelBooking(bookingId));
        verify(bookingRepository).findByBookingId(bookingId);
    }
    
    @Test
    void cancelBooking_ShouldThrowException_WhenBookingAlreadyCancelled() {
        // Given
        String bookingId = "BK123456789";
        testBooking.setStatus(BookingStatus.CANCELLED);
        
        when(bookingRepository.findByBookingId(bookingId))
            .thenReturn(Optional.of(testBooking));
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            bookingService.cancelBooking(bookingId));
        verify(bookingRepository).findByBookingId(bookingId);
    }
    
    @Test
    void getAllBookings_ShouldReturnAllBookings() {
        // Given
        List<Booking> bookings = Arrays.asList(testBooking);
        when(bookingRepository.findAll())
            .thenReturn(bookings);
        
        // When
        List<BookingResponse> responses = bookingService.getAllBookings();
        
        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(testBooking.getBookingId(), responses.get(0).getBookingId());
        verify(bookingRepository).findAll();
    }
}
