package org.example.repository;

import org.example.model.Seat;
import org.example.enums.SeatStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    
    List<Seat> findByFlightId(Long flightId);
    
    List<Seat> findByFlightIdAndStatus(Long flightId, SeatStatus status);
    
    @Query("SELECT s FROM Seat s WHERE s.flightId = :flightId AND s.status = :status")
    List<Seat> findAvailableSeatsByFlightId(@Param("flightId") Long flightId, 
                                          @Param("status") SeatStatus status);
    
    @Query("SELECT COUNT(s) FROM Seat s WHERE s.flightId = :flightId AND s.status = :status")
    Long countByFlightIdAndStatus(@Param("flightId") Long flightId, 
                                 @Param("status") SeatStatus status);
    
    Optional<Seat> findByFlightIdAndSeatId(Long flightId, String seatId);
    
    @Query("SELECT s FROM Seat s WHERE s.flightId = :flightId AND s.seatId = :seatId AND s.status = :status")
    Optional<Seat> findAvailableSeatByFlightIdAndSeatId(@Param("flightId") Long flightId, 
                                                      @Param("seatId") String seatId, 
                                                      @Param("status") SeatStatus status);
    
    @Query("SELECT s FROM Seat s WHERE s.flightId = :flightId AND s.status IN (:statuses)")
    List<Seat> findByFlightIdAndStatusIn(@Param("flightId") Long flightId, 
                                        @Param("statuses") List<SeatStatus> statuses);
    
    @Query("SELECT COUNT(s) FROM Seat s WHERE s.flightId = :flightId AND s.status = :status")
    Long countAvailableSeatsByFlightId(@Param("flightId") Long flightId, 
                                      @Param("status") SeatStatus status);
}
