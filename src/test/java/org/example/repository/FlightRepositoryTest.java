package org.example.repository;

import org.example.model.Flight;
import org.example.enums.FlightStatus;
import org.example.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class FlightRepositoryTest {
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private FlightRepository flightRepository;
    
    private Flight testFlight;
    
    @BeforeEach
    void setUp() {
        testFlight = TestDataBuilder.createFlight();
        testFlight.setId(null); // Let JPA generate the ID
        testFlight = entityManager.persistAndFlush(testFlight);
    }
    
    @Test
    void findById_ShouldReturnFlight_WhenFlightExists() {
        // When
        Optional<Flight> result = flightRepository.findById(testFlight.getId());
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(testFlight.getId(), result.get().getId());
        assertEquals(testFlight.getFlightNumber(), result.get().getFlightNumber());
    }
    
    @Test
    void findById_ShouldReturnEmpty_WhenFlightNotExists() {
        // When
        Optional<Flight> result = flightRepository.findById(999L);
        
        // Then
        assertFalse(result.isPresent());
    }
    
    @Test
    void findByFlightNumber_ShouldReturnFlight_WhenFlightExists() {
        // When
        Optional<Flight> result = flightRepository.findByFlightNumber(testFlight.getFlightNumber());
        
        // Then
        assertTrue(result.isPresent());
        assertEquals(testFlight.getFlightNumber(), result.get().getFlightNumber());
    }
    
    @Test
    void findByFlightNumber_ShouldReturnEmpty_WhenNoFlightsExist() {
        // When
        Optional<Flight> result = flightRepository.findByFlightNumber("NONEXISTENT");
        
        // Then
        assertFalse(result.isPresent());
    }
    
    @Test
    void findByFromAndToAndDepartureTimeBetween_ShouldReturnFlights_WhenFlightsExist() {
        // Given
        String from = "New York";
        String to = "Los Angeles";
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(2);
        
        Flight flight2 = TestDataBuilder.createFlight(2L, "FL002", from, to);
        flight2.setId(null); // Let JPA generate the ID
        flight2.setDepartureTime(startDate.plusHours(1));
        entityManager.persistAndFlush(flight2);
        
        // When
        List<Flight> result = flightRepository.findByFromAndToAndDepartureTimeBetween(from, to, startDate, endDate);
        
        // Then
        assertTrue(result.size() >= 1);
        assertTrue(result.stream().allMatch(f -> f.getFrom().equals(from) && f.getTo().equals(to)));
    }
    
    @Test
    void findByFromAndToAndDepartureTimeBetween_ShouldReturnEmptyList_WhenNoFlightsExist() {
        // Given
        String from = "Paris";
        String to = "Tokyo";
        LocalDateTime startDate = LocalDateTime.now().plusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(2);
        
        // When
        List<Flight> result = flightRepository.findByFromAndToAndDepartureTimeBetween(from, to, startDate, endDate);
        
        // Then
        assertTrue(result.isEmpty());
    }
    
    @Test
    void findByStatus_ShouldReturnFlights_WhenFlightsExist() {
        // Given
        FlightStatus status = FlightStatus.SCHEDULED;
        
        Flight flight2 = TestDataBuilder.createFlight(2L, "FL002", "Chicago", "Miami");
        flight2.setId(null); // Let JPA generate the ID
        flight2.setStatus(status);
        entityManager.persistAndFlush(flight2);
        
        // When
        List<Flight> result = flightRepository.findByStatus(status);
        
        // Then
        assertTrue(result.size() >= 1);
        assertTrue(result.stream().allMatch(f -> f.getStatus().equals(status)));
    }
    
    @Test
    void findByStatus_ShouldReturnEmptyList_WhenNoFlightsExist() {
        // When
        List<Flight> result = flightRepository.findByStatus(FlightStatus.CANCELLED);
        
        // Then
        assertTrue(result.isEmpty());
    }
    
    @Test
    void findAvailableFlights_ShouldReturnFlights_WhenFlightsMatch() {
        // Given
        String from = "New York";
        String to = "Los Angeles";
        LocalDateTime date = LocalDateTime.now().plusDays(1);
        FlightStatus status = FlightStatus.SCHEDULED;
        int passengers = 2;
        LocalDateTime startOfDay = date.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        
        // When
        List<Flight> result = flightRepository.findAvailableFlights(
            from, to, startOfDay, endOfDay, status, passengers);
        
        // Then
        // The test flight should match the criteria
        assertTrue(result.size() >= 1);
        assertTrue(result.stream().anyMatch(f -> 
            f.getFrom().equals(from) && 
            f.getTo().equals(to) && 
            f.getStatus().equals(status) &&
            f.getAvailableSeats() >= passengers));
    }
    
    @Test
    void findAvailableFlights_ShouldReturnEmptyList_WhenNoFlightsMatch() {
        // Given
        String from = "Paris";
        String to = "Tokyo";
        LocalDateTime date = LocalDateTime.now().plusDays(1);
        FlightStatus status = FlightStatus.SCHEDULED;
        int passengers = 2;
        
        // When
        List<Flight> result = flightRepository.findAvailableFlights(
            from, to, date, date.plusHours(23).plusMinutes(59), status, passengers);
        
        // Then
        assertTrue(result.isEmpty());
    }
    
    @Test
    void findByFlightNumber_ShouldReturnTrue_WhenFlightExists() {
        // When
        Optional<Flight> result = flightRepository.findByFlightNumber(testFlight.getFlightNumber());
        
        // Then
        assertTrue(result.isPresent());
    }
    
    @Test
    void findByFlightNumber_ShouldReturnFalse_WhenFlightNotExists() {
        // When
        Optional<Flight> result = flightRepository.findByFlightNumber("NONEXISTENT");
        
        // Then
        assertFalse(result.isPresent());
    }
    
    @Test
    void countByStatus_ShouldReturnCorrectCount() {
        // Given
        FlightStatus status = FlightStatus.SCHEDULED;
        
        Flight flight2 = TestDataBuilder.createFlight(2L, "FL002", "Chicago", "Miami");
        flight2.setId(null); // Let JPA generate the ID
        flight2.setStatus(status);
        entityManager.persistAndFlush(flight2);
        
        // When
        long count = flightRepository.countByStatus(status);
        
        // Then
        assertEquals(2, count);
    }
    
    @Test
    void deleteById_ShouldDeleteFlight_WhenFlightExists() {
        // Given
        Long flightId = testFlight.getId();
        
        // When
        flightRepository.deleteById(flightId);
        
        // Then
        Optional<Flight> result = flightRepository.findById(flightId);
        assertFalse(result.isPresent());
    }
}