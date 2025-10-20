package org.example.controller;

import org.example.dto.FlightRequest;
import org.example.dto.FlightResponse;
import org.example.dto.SearchFlightRequest;
import org.example.service.FlightService;
import org.example.enums.FlightStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/flights")
public class FlightController {
    
    @Autowired
    private FlightService flightService;
    
    @PostMapping("/admin/add")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FlightResponse> addFlight(@Valid @RequestBody FlightRequest request) {
        FlightResponse response = flightService.addFlight(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<FlightResponse> getFlightById(@PathVariable Long id) {
        FlightResponse response = flightService.getFlightById(id);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/search")
    public ResponseEntity<List<FlightResponse>> searchFlights(@Valid @RequestBody SearchFlightRequest request) {
        List<FlightResponse> responses = flightService.searchFlights(request);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping
    public ResponseEntity<List<FlightResponse>> getAllFlights() {
        List<FlightResponse> responses = flightService.getAllFlights();
        return ResponseEntity.ok(responses);
    }
    
    @PutMapping("/admin/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FlightResponse> updateFlightStatus(
            @PathVariable Long id, 
            @RequestParam FlightStatus status) {
        FlightResponse response = flightService.updateFlightStatus(id, status);
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteFlight(@PathVariable Long id) {
        flightService.deleteFlight(id);
        return ResponseEntity.noContent().build();
    }
}
