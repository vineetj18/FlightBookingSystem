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
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class FlightService {
    
    @Autowired
    private FlightRepository flightRepository;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Value("${app.queue.seat-creation-queue:seat.creation.queue}")
    private String seatCreationQueue;
    
    public FlightResponse addFlight(FlightRequest request) {
        // Create flight entity using builder pattern
        Flight flight = Flight.builder()
                .flightNumber(request.getFlightNumber())
                .from(request.getFrom())
                .to(request.getTo())
                .flightMetadata(request.getFlightMetadata())
                .departureTime(request.getDepartureTime())
                .arrivalTime(request.getArrivalTime())
                .price(request.getPrice())
                .maxPassengers(request.getMaxPassengers())
                .availableSeats(request.getMaxPassengers())
                .build();
        
        // Save flight
        Flight savedFlight = flightRepository.save(flight);
        
        // Send message to queue for seat creation
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
            request.getDate(),
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
        SeatCreationMessage message = new SeatCreationMessage(flightId, maxPassengers);
        rabbitTemplate.convertAndSend(seatCreationQueue, message);
    }
    
    private FlightResponse convertToResponse(Flight flight) {
        return FlightResponse.builder()
                .id(flight.getId())
                .flightNumber(flight.getFlightNumber())
                .from(flight.getFrom())
                .to(flight.getTo())
                .flightMetadata(flight.getFlightMetadata())
                .departureTime(flight.getDepartureTime())
                .arrivalTime(flight.getArrivalTime())
                .status(flight.getStatus())
                .price(flight.getPrice())
                .maxPassengers(flight.getMaxPassengers())
                .availableSeats(flight.getAvailableSeats())
                .createdAt(flight.getCreatedAt())
                .updatedAt(flight.getUpdatedAt())
                .build();
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
