package org.example.controller;

import org.example.dto.BookingRequest;
import org.example.dto.BookingResponse;
import org.example.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/bookings")
public class BookingController {
    
    @Autowired
    private BookingService bookingService;
    
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        BookingResponse response = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{bookingId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable String bookingId) {
        BookingResponse response = bookingService.getBookingById(bookingId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/pnr/{pnr}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BookingResponse> getBookingByPnr(@PathVariable String pnr) {
        BookingResponse response = bookingService.getBookingByPnr(pnr);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/user/{bookedBy}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<BookingResponse>> getBookingsByUser(@PathVariable String bookedBy) {
        List<BookingResponse> responses = bookingService.getBookingsByUser(bookedBy);
        return ResponseEntity.ok(responses);
    }
    
    @PutMapping("/{bookingId}/cancel")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable String bookingId) {
        BookingResponse response = bookingService.cancelBooking(bookingId);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BookingResponse>> getAllBookings() {
        List<BookingResponse> responses = bookingService.getAllBookings();
        return ResponseEntity.ok(responses);
    }
}
