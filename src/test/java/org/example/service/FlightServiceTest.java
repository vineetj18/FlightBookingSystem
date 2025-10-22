package org.example.service;

import org.example.dto.FlightRequest;
import org.example.dto.FlightResponse;
import org.example.dto.SearchFlightRequest;
import org.example.exception.FlightNotFoundException;
import org.example.model.Flight;
import org.example.repository.FlightRepository;
import org.example.enums.FlightStatus;
import org.example.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlightServiceTest {
    
    @Mock
    private FlightRepository flightRepository;
    
    @Mock
    private RabbitTemplate rabbitTemplate;
    
    @Mock
    private SeatCreationService seatCreationService;
    
    @InjectMocks
    private FlightService flightService;
    
    private Flight testFlight;
    private FlightRequest testFlightRequest;
    
    @BeforeEach
    void setUp() {
        testFlight = TestDataBuilder.createFlight();
        testFlightRequest = TestDataBuilder.createFlightRequest();
    }
    
    @Test
    void addFlight_ShouldCreateFlightSuccessfully() {
        // Given
        when(flightRepository.save(any(Flight.class))).thenReturn(testFlight);
        
        // When
        FlightResponse response = flightService.addFlight(testFlightRequest);
        
        // Then
        assertNotNull(response);
        assertEquals(testFlight.getFlightNumber(), response.getFlightNumber());
        assertEquals(testFlight.getFrom(), response.getFrom());
        assertEquals(testFlight.getTo(), response.getTo());
        assertEquals(testFlight.getPrice(), response.getPrice());
        assertEquals(testFlight.getMaxPassengers(), response.getMaxPassengers());
        
        verify(flightRepository).save(any(Flight.class));
        verify(seatCreationService).createSeatsForFlight(eq(testFlight.getId()), eq(testFlight.getMaxPassengers()));
    }
    
    @Test
    void getFlightById_ShouldReturnFlight_WhenFlightExists() {
        // Given
        Long flightId = 1L;
        when(flightRepository.findById(flightId)).thenReturn(Optional.of(testFlight));
        
        // When
        FlightResponse response = flightService.getFlightById(flightId);
        
        // Then
        assertNotNull(response);
        assertEquals(testFlight.getId(), response.getId());
        assertEquals(testFlight.getFlightNumber(), response.getFlightNumber());
        verify(flightRepository).findById(flightId);
    }
    
    @Test
    void getFlightById_ShouldThrowException_WhenFlightNotFound() {
        // Given
        Long flightId = 999L;
        when(flightRepository.findById(flightId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(FlightNotFoundException.class, () -> flightService.getFlightById(flightId));
        verify(flightRepository).findById(flightId);
    }
    
    @Test
    void searchFlights_ShouldReturnMatchingFlights() {
        // Given
        SearchFlightRequest searchRequest = TestDataBuilder.createSearchRequest();
        List<Flight> flights = Arrays.asList(testFlight);
        
        when(flightRepository.findAvailableFlights(
            eq(searchRequest.getFrom()),
            eq(searchRequest.getTo()),
            any(LocalDateTime.class),
            any(LocalDateTime.class),
            eq(FlightStatus.SCHEDULED),
            eq(searchRequest.getPassengers())
        )).thenReturn(flights);
        
        // When
        List<FlightResponse> responses = flightService.searchFlights(searchRequest);
        
        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(testFlight.getFlightNumber(), responses.get(0).getFlightNumber());
        verify(flightRepository).findAvailableFlights(
            eq(searchRequest.getFrom()),
            eq(searchRequest.getTo()),
            any(LocalDateTime.class),
            any(LocalDateTime.class),
            eq(FlightStatus.SCHEDULED),
            eq(searchRequest.getPassengers())
        );
    }
    
    @Test
    void getAllFlights_ShouldReturnAllFlights() {
        // Given
        List<Flight> flights = Arrays.asList(testFlight);
        when(flightRepository.findAll()).thenReturn(flights);
        
        // When
        List<FlightResponse> responses = flightService.getAllFlights();
        
        // Then
        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals(testFlight.getFlightNumber(), responses.get(0).getFlightNumber());
        verify(flightRepository).findAll();
    }
    
    @Test
    void updateFlightStatus_ShouldUpdateStatus_WhenFlightExists() {
        // Given
        Long flightId = 1L;
        FlightStatus newStatus = FlightStatus.ON_TIME;
        testFlight.setStatus(newStatus);
        
        when(flightRepository.findById(flightId)).thenReturn(Optional.of(testFlight));
        when(flightRepository.save(any(Flight.class))).thenReturn(testFlight);
        
        // When
        FlightResponse response = flightService.updateFlightStatus(flightId, newStatus);
        
        // Then
        assertNotNull(response);
        assertEquals(newStatus, response.getStatus());
        verify(flightRepository).findById(flightId);
        verify(flightRepository).save(any(Flight.class));
    }
    
    @Test
    void updateFlightStatus_ShouldThrowException_WhenFlightNotFound() {
        // Given
        Long flightId = 999L;
        FlightStatus newStatus = FlightStatus.ON_TIME;
        
        when(flightRepository.findById(flightId)).thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(FlightNotFoundException.class, () -> 
            flightService.updateFlightStatus(flightId, newStatus));
        verify(flightRepository).findById(flightId);
        verify(flightRepository, never()).save(any(Flight.class));
    }
    
    @Test
    void deleteFlight_ShouldDeleteFlight_WhenFlightExists() {
        // Given
        Long flightId = 1L;
        when(flightRepository.existsById(flightId)).thenReturn(true);
        
        // When
        flightService.deleteFlight(flightId);
        
        // Then
        verify(flightRepository).existsById(flightId);
        verify(flightRepository).deleteById(flightId);
    }
    
    @Test
    void deleteFlight_ShouldThrowException_WhenFlightNotFound() {
        // Given
        Long flightId = 999L;
        when(flightRepository.existsById(flightId)).thenReturn(false);
        
        // When & Then
        assertThrows(FlightNotFoundException.class, () -> flightService.deleteFlight(flightId));
        verify(flightRepository).existsById(flightId);
        verify(flightRepository, never()).deleteById(anyLong());
    }
}
