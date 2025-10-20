package org.example.repository;

import org.example.model.BookingSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BookingSeatRepository extends JpaRepository<BookingSeat, Long> {
    
    List<BookingSeat> findByBookingId(Long bookingId);
    
    List<BookingSeat> findBySeatId(String seatId);
    
    List<BookingSeat> findByPassengerName(String passengerName);
}
