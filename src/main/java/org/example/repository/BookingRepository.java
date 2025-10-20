package org.example.repository;

import org.example.model.Booking;
import org.example.enums.BookingStatus;
import org.example.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    Optional<Booking> findByBookingId(String bookingId);
    
    Optional<Booking> findByPnr(String pnr);
    
    List<Booking> findByBookedBy(String bookedBy);
    
    List<Booking> findByFlightId(Long flightId);
    
    List<Booking> findByStatus(BookingStatus status);
    
    List<Booking> findByPaymentStatus(PaymentStatus paymentStatus);
    
    @Query("SELECT b FROM Booking b WHERE b.flightId = :flightId AND b.seatId = :seatId")
    Optional<Booking> findByFlightIdAndSeatId(@Param("flightId") Long flightId, 
                                              @Param("seatId") String seatId);
    
    @Query("SELECT b FROM Booking b WHERE b.bookedBy = :bookedBy AND b.status = :status")
    List<Booking> findByBookedByAndStatus(@Param("bookedBy") String bookedBy, 
                                        @Param("status") BookingStatus status);
    
    @Query("SELECT b FROM Booking b WHERE b.flightId = :flightId AND b.status = :status")
    List<Booking> findByFlightIdAndStatus(@Param("flightId") Long flightId, 
                                        @Param("status") BookingStatus status);
    
    @Query("SELECT b FROM Booking b WHERE b.createdAt >= :startDate AND b.createdAt <= :endDate")
    List<Booking> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.flightId = :flightId AND b.status = :status")
    Long countByFlightIdAndStatus(@Param("flightId") Long flightId, 
                                 @Param("status") BookingStatus status);
    
    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.paymentStatus = :paymentStatus")
    List<Booking> findByStatusAndPaymentStatus(@Param("status") BookingStatus status, 
                                              @Param("paymentStatus") PaymentStatus paymentStatus);
}
