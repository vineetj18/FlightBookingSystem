package org.example.repository;

import org.example.model.Flight;
import org.example.enums.FlightStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {
    
    Optional<Flight> findByFlightNumber(String flightNumber);
    
    List<Flight> findByFromAndToAndDepartureDateBetween(
            String from, String to, LocalDateTime startDate, LocalDateTime endDate);
    
    List<Flight> findByFromAndToAndDepartureDateBetweenAndStatus(
            String from, String to, LocalDateTime startDate, LocalDateTime endDate, FlightStatus status);
    
    @Query("SELECT f FROM Flight f WHERE f.from = :from AND f.to = :to " +
           "AND DATE(f.departureDate) = DATE(:date) AND f.status = :status " +
           "AND f.availableSeats >= :passengers")
    List<Flight> findAvailableFlights(@Param("from") String from, 
                                    @Param("to") String to, 
                                    @Param("date") LocalDateTime date, 
                                    @Param("status") FlightStatus status, 
                                    @Param("passengers") Integer passengers);
    
    @Query("SELECT f FROM Flight f WHERE f.from = :from AND f.to = :to " +
           "AND f.departureDate >= :startDate AND f.departureDate <= :endDate " +
           "AND f.status = :status AND f.availableSeats >= :passengers")
    List<Flight> findAvailableFlightsInRange(@Param("from") String from, 
                                           @Param("to") String to, 
                                           @Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate, 
                                           @Param("status") FlightStatus status, 
                                           @Param("passengers") Integer passengers);
    
    List<Flight> findByStatus(FlightStatus status);
    
    @Query("SELECT COUNT(f) FROM Flight f WHERE f.status = :status")
    Long countByStatus(@Param("status") FlightStatus status);
}
