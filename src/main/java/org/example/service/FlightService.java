package org.example.service;

import org.example.dto.FlightRequest;
import org.example.dto.FlightResponse;
import org.example.dto.SearchFlightRequest;
import org.example.exception.FlightNotFoundException;
import org.example.model.Flight;
import org.example.repository.FlightRepository;
import org.example.enums.FlightStatus;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FlightService {
    
    private static final Logger logger = LoggerFactory.getLogger(FlightService.class);
    
    @Autowired
    private FlightRepository flightRepository;
    
    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;
    
    @Autowired
    private SeatCreationService seatCreationService;
    
    @Value("${app.queue.seat-creation-queue:seat.creation.queue}")
    private String seatCreationQueue;
    
    public FlightResponse addFlight(FlightRequest request) {
        // Create flight entity using builder pattern
        Flight flight = new Flight();
        flight.setFlightNumber(request.getFlightNumber());
        flight.setFrom(request.getFrom());
        flight.setTo(request.getTo());
        flight.setFlightMetadata(request.getFlightMetadata());
        flight.setDepartureTime(request.getDepartureTime());
        flight.setArrivalTime(request.getArrivalTime());
        flight.setPrice(request.getPrice());
        flight.setMaxPassengers(request.getMaxPassengers());
        flight.setAvailableSeats(request.getMaxPassengers());
        
        // Save flight
        Flight savedFlight = flightRepository.save(flight);
        
        // Create seats for the flight
        seatCreationService.createSeatsForFlight(savedFlight.getId(), savedFlight.getMaxPassengers());
        
        // Send message to queue for seat creation (if RabbitMQ is available)
        sendSeatCreationMessage(savedFlight.getId(), savedFlight.getMaxPassengers());
        
        return convertToResponse(savedFlight);
    }
    
    @Cacheable(value = "flights", key = "#id")
    public FlightResponse getFlightById(Long id) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new FlightNotFoundException(id));
        return convertToResponse(flight);
    }
    
    public List<FlightResponse> searchFlights(SearchFlightRequest request) {
        LocalDateTime startDate = request.getDate().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endDate = request.getDate().withHour(23).withMinute(59).withSecond(59);
        
        List<Flight> flights = flightRepository.findAvailableFlights(
            request.getFrom(),
            request.getTo(),
            startDate,
            endDate,
            FlightStatus.SCHEDULED,
            request.getPassengers()
        );
        
        return flights.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public List<FlightResponse> getAllFlights() {
        List<Flight> flights = flightRepository.findAll();
        return flights.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }
    
    public FlightResponse updateFlightStatus(Long id, FlightStatus status) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new FlightNotFoundException(id));
        
        flight.setStatus(status);
        Flight updatedFlight = flightRepository.save(flight);
        
        return convertToResponse(updatedFlight);
    }
    
    public void deleteFlight(Long id) {
        if (!flightRepository.existsById(id)) {
            throw new FlightNotFoundException(id);
        }
        flightRepository.deleteById(id);
    }
    
    private void sendSeatCreationMessage(Long flightId, Integer maxPassengers) {
        if (rabbitTemplate != null) {
            try {
                SeatCreationMessage message = new SeatCreationMessage(flightId, maxPassengers);
                rabbitTemplate.convertAndSend(seatCreationQueue, message);
                logger.info("Seat creation message sent for flight ID: {}", flightId);
            } catch (Exception e) {
                logger.warn("Failed to send seat creation message for flight ID: {}. Error: {}", flightId, e.getMessage());
                // Continue execution - this is not critical for basic functionality
            }
        } else {
            logger.info("RabbitMQ not available - skipping seat creation message for flight ID: {}", flightId);
        }
    }
    
    private FlightResponse convertToResponse(Flight flight) {
        FlightResponse response = new FlightResponse();
        response.setId(flight.getId());
        response.setFlightNumber(flight.getFlightNumber());
        response.setFrom(flight.getFrom());
        response.setTo(flight.getTo());
        response.setFlightMetadata(flight.getFlightMetadata());
        response.setDepartureTime(flight.getDepartureTime());
        response.setArrivalTime(flight.getArrivalTime());
        response.setStatus(flight.getStatus());
        response.setPrice(flight.getPrice());
        response.setMaxPassengers(flight.getMaxPassengers());
        response.setAvailableSeats(flight.getAvailableSeats());
        response.setCreatedAt(flight.getCreatedAt());
        response.setUpdatedAt(flight.getUpdatedAt());
        return response;
    }
    
    // Inner class for seat creation message
    public static class SeatCreationMessage {
        private Long flightId;
        private Integer maxPassengers;
        
        public SeatCreationMessage() {}
        
        public SeatCreationMessage(Long flightId, Integer maxPassengers) {
            this.flightId = flightId;
            this.maxPassengers = maxPassengers;
        }
        
        public Long getFlightId() {
            return flightId;
        }
        
        public void setFlightId(Long flightId) {
            this.flightId = flightId;
        }
        
        public Integer getMaxPassengers() {
            return maxPassengers;
        }
        
        public void setMaxPassengers(Integer maxPassengers) {
            this.maxPassengers = maxPassengers;
        }
    }
}
