package org.example.repository;

import org.example.model.Booking;
import org.example.enums.BookingStatus;
import org.example.enums.PaymentStatus;
import org.example.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class BookingRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    private Booking testBooking;
    
    @BeforeEach
    void setUp() {
        testBooking = TestDataBuilder.createBooking();
        testBooking.setId(null); // Let JPA generate the ID
        testBooking = entityManager.persistAndFlush(testBooking);
    }
    
    @Test
    void findByBookingId_ShouldReturnBooking_WhenBookingExists() {
        // When
        Optional<Booking> result = bookingRepository.findByBookingId(testBooking.getBookingId());
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(testBooking.getId(), result.get().getId());
        assertEquals(testBooking.getBookingId(), result.get().getBookingId());
    }
    
    @Test
    void findByBookingId_ShouldReturnEmpty_WhenBookingNotExists() {
        // When
        Optional<Booking> result = bookingRepository.findByBookingId("NONEXISTENT");
        
        // Then
        assertFalse(result.isPresent());
    }
    
    @Test
    void findByPnr_ShouldReturnBooking_WhenBookingExists() {
        // When
        Optional<Booking> result = bookingRepository.findByPnr(testBooking.getPnr());
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(testBooking.getId(), result.get().getId());
        assertEquals(testBooking.getPnr(), result.get().getPnr());
    }
    
    @Test
    void findByPnr_ShouldReturnEmpty_WhenBookingNotExists() {
        // When
        Optional<Booking> result = bookingRepository.findByPnr("NONEXISTENT");
        
        // Then
        assertFalse(result.isPresent());
    }
    
    @Test
    void findByBookedBy_ShouldReturnBookings_WhenBookingsExist() {
        // Given
        String userEmail = "user@example.com";
        
        Booking booking2 = TestDataBuilder.createBooking();
        booking2.setId(null); // Let JPA generate the ID
        booking2.setBookingId("BK002");
        booking2.setBookedBy(userEmail);
        entityManager.persistAndFlush(booking2);
        
        // When
        List<Booking> result = bookingRepository.findByBookedBy(userEmail);
        
        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(b -> b.getBookedBy().equals(userEmail)));
    }
    
    @Test
    void findByBookedBy_ShouldReturnEmptyList_WhenNoBookingsExist() {
        // When
        List<Booking> result = bookingRepository.findByBookedBy("nonexistent@example.com");
        
        // Then
        assertTrue(result.isEmpty());
    }
    
    @Test
    void findByFlightId_ShouldReturnBookings_WhenBookingsExist() {
        // Given
        Long flightId = 1L;
        
        Booking booking2 = TestDataBuilder.createBooking();
        booking2.setId(null); // Let JPA generate the ID
        booking2.setBookingId("BK002");
        booking2.setFlightId(flightId);
        entityManager.persistAndFlush(booking2);
        
        // When
        List<Booking> result = bookingRepository.findByFlightId(flightId);
        
        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(b -> b.getFlightId().equals(flightId)));
    }
    
    @Test
    void findByFlightId_ShouldReturnEmptyList_WhenNoBookingsExist() {
        // When
        List<Booking> result = bookingRepository.findByFlightId(999L);
        
        // Then
        assertTrue(result.isEmpty());
    }
    
    @Test
    void findByStatus_ShouldReturnBookings_WhenBookingsExist() {
        // Given
        BookingStatus status = BookingStatus.CONFIRMED;
        
        Booking booking2 = TestDataBuilder.createBooking();
        booking2.setId(null); // Let JPA generate the ID
        booking2.setBookingId("BK002");
        booking2.setStatus(status);
        entityManager.persistAndFlush(booking2);
        
        // When
        List<Booking> result = bookingRepository.findByStatus(status);
        
        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(b -> b.getStatus().equals(status)));
    }
    
    @Test
    void findByStatus_ShouldReturnEmptyList_WhenNoBookingsExist() {
        // When
        List<Booking> result = bookingRepository.findByStatus(BookingStatus.CANCELLED);
        
        // Then
        assertTrue(result.isEmpty());
    }
    
    @Test
    void findByPaymentStatus_ShouldReturnBookings_WhenBookingsExist() {
        // Given
        PaymentStatus paymentStatus = PaymentStatus.COMPLETED;
        
        Booking booking2 = TestDataBuilder.createBooking();
        booking2.setId(null); // Let JPA generate the ID
        booking2.setBookingId("BK002");
        booking2.setPaymentStatus(paymentStatus);
        entityManager.persistAndFlush(booking2);
        
        // When
        List<Booking> result = bookingRepository.findByPaymentStatus(paymentStatus);
        
        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(b -> b.getPaymentStatus().equals(paymentStatus)));
    }
    
    @Test
    void findByPaymentStatus_ShouldReturnEmptyList_WhenNoBookingsExist() {
        // When
        List<Booking> result = bookingRepository.findByPaymentStatus(PaymentStatus.FAILED);
        
        // Then
        assertTrue(result.isEmpty());
    }
    
    @Test
    void findByBookedByAndStatus_ShouldReturnBookings_WhenBookingsExist() {
        // Given
        String userEmail = "user@example.com";
        BookingStatus status = BookingStatus.CONFIRMED;
        
        Booking booking2 = TestDataBuilder.createBooking();
        booking2.setId(null); // Let JPA generate the ID
        booking2.setBookingId("BK002");
        booking2.setBookedBy(userEmail);
        booking2.setStatus(status);
        entityManager.persistAndFlush(booking2);
        
        // When
        List<Booking> result = bookingRepository.findByBookedByAndStatus(userEmail, status);
        
        // Then
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(b -> 
            b.getBookedBy().equals(userEmail) && b.getStatus().equals(status)));
    }
    
    @Test
    void findByBookedByAndStatus_ShouldReturnEmptyList_WhenNoBookingsExist() {
        // When
        List<Booking> result = bookingRepository.findByBookedByAndStatus(
            "nonexistent@example.com", BookingStatus.CANCELLED);
        
        // Then
        assertTrue(result.isEmpty());
    }
    
    @Test
    void findByCreatedAtBetween_ShouldReturnBookings_WhenBookingsExist() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);
        
        // When
        List<Booking> result = bookingRepository.findByCreatedAtBetween(startDate, endDate);
        
        // Then
        assertTrue(result.size() >= 1);
        assertTrue(result.stream().allMatch(b -> 
            b.getCreatedAt().isAfter(startDate) && b.getCreatedAt().isBefore(endDate)));
    }
    
    @Test
    void findByCreatedAtBetween_ShouldReturnEmptyList_WhenNoBookingsExist() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(2);
        
        // When
        List<Booking> result = bookingRepository.findByCreatedAtBetween(startDate, endDate);
        
        // Then
        assertTrue(result.isEmpty());
    }
    
    @Test
    void findByBookingId_ShouldReturnTrue_WhenBookingExists() {
        // When
        Optional<Booking> result = bookingRepository.findByBookingId(testBooking.getBookingId());
        
        // Then
        assertTrue(result.isPresent());
    }
    
    @Test
    void findByBookingId_ShouldReturnFalse_WhenBookingNotExists() {
        // When
        Optional<Booking> result = bookingRepository.findByBookingId("NONEXISTENT");
        
        // Then
        assertFalse(result.isPresent());
    }
    
    @Test
    void findByPnr_ShouldReturnTrue_WhenBookingExists() {
        // When
        Optional<Booking> result = bookingRepository.findByPnr(testBooking.getPnr());
        
        // Then
        assertTrue(result.isPresent());
    }
    
    @Test
    void findByPnr_ShouldReturnFalse_WhenBookingNotExists() {
        // When
        Optional<Booking> result = bookingRepository.findByPnr("NONEXISTENT");
        
        // Then
        assertFalse(result.isPresent());
    }
}