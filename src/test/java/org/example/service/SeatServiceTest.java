package org.example.service;

import org.example.exception.SeatNotAvailableException;
import org.example.model.Seat;
import org.example.repository.SeatRepository;
import org.example.enums.SeatStatus;
import org.example.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatServiceTest {
    
    @Mock
    private SeatRepository seatRepository;
    
    @InjectMocks
    private SeatService seatService;
    
    private List<Seat> testSeats;
    private Long testFlightId;
    
    @BeforeEach
    void setUp() {
        testFlightId = 1L;
        testSeats = TestDataBuilder.createSeatsForFlight(testFlightId, 5);
    }
    
    @Test
    void getAvailableSeatsSequentially_ShouldReturnAvailableSeats_WhenSeatsExist() {
        // Given
        int requestedSeats = 3;
        when(seatRepository.findAvailableSeatsByFlightId(testFlightId, SeatStatus.AVAILABLE))
            .thenReturn(testSeats);
        
        // When
        List<Seat> result = seatService.getAvailableSeatsSequentially(testFlightId, requestedSeats);
        
        // Then
        assertNotNull(result);
        assertEquals(requestedSeats, result.size());
        assertEquals("A1", result.get(0).getSeatId());
        assertEquals("A2", result.get(1).getSeatId());
        assertEquals("A3", result.get(2).getSeatId());
        
        verify(seatRepository).findAvailableSeatsByFlightId(testFlightId, SeatStatus.AVAILABLE);
    }
    
    @Test
    void getAvailableSeatsSequentially_ShouldThrowException_WhenNotEnoughSeats() {
        // Given
        int requestedSeats = 10;
        when(seatRepository.findAvailableSeatsByFlightId(testFlightId, SeatStatus.AVAILABLE))
            .thenReturn(testSeats);
        
        // When & Then
        assertThrows(SeatNotAvailableException.class, () -> 
            seatService.getAvailableSeatsSequentially(testFlightId, requestedSeats));
        verify(seatRepository).findAvailableSeatsByFlightId(testFlightId, SeatStatus.AVAILABLE);
    }
    
    @Test
    void getAvailableSeatsSequentially_ShouldReturnEmptyList_WhenNoSeatsAvailable() {
        // Given
        int requestedSeats = 3;
        when(seatRepository.findAvailableSeatsByFlightId(testFlightId, SeatStatus.AVAILABLE))
            .thenReturn(Arrays.asList());
        
        // When & Then
        assertThrows(SeatNotAvailableException.class, () -> 
            seatService.getAvailableSeatsSequentially(testFlightId, requestedSeats));
        verify(seatRepository).findAvailableSeatsByFlightId(testFlightId, SeatStatus.AVAILABLE);
    }
    
    @Test
    void lockSeat_ShouldLockSeat_WhenSeatExists() {
        // Given
        String seatId = "A1";
        Seat seat = TestDataBuilder.createSeat();
        seat.setSeatId(seatId);
        seat.setStatus(SeatStatus.AVAILABLE);
        
        when(seatRepository.findAvailableSeatByFlightIdAndSeatId(testFlightId, seatId, SeatStatus.AVAILABLE))
            .thenReturn(Optional.of(seat));
        when(seatRepository.save(any(Seat.class)))
            .thenReturn(seat);
        
        // When
        Seat result = seatService.lockSeat(testFlightId, seatId);
        
        // Then
        assertNotNull(result);
        assertEquals(SeatStatus.LOCKED, result.getStatus());
        verify(seatRepository).findAvailableSeatByFlightIdAndSeatId(testFlightId, seatId, SeatStatus.AVAILABLE);
        verify(seatRepository).save(seat);
    }
    
    @Test
    void lockSeat_ShouldThrowException_WhenSeatNotFound() {
        // Given
        String seatId = "Z99";
        when(seatRepository.findAvailableSeatByFlightIdAndSeatId(testFlightId, seatId, SeatStatus.AVAILABLE))
            .thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            seatService.lockSeat(testFlightId, seatId));
        verify(seatRepository).findAvailableSeatByFlightIdAndSeatId(testFlightId, seatId, SeatStatus.AVAILABLE);
        verify(seatRepository, never()).save(any(Seat.class));
    }
    
    @Test
    void lockSeat_ShouldThrowException_WhenSeatAlreadyOccupied() {
        // Given
        String seatId = "A1";
        Seat seat = TestDataBuilder.createSeat();
        seat.setSeatId(seatId);
        seat.setStatus(SeatStatus.OCCUPIED);
        
        when(seatRepository.findAvailableSeatByFlightIdAndSeatId(testFlightId, seatId, SeatStatus.AVAILABLE))
            .thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            seatService.lockSeat(testFlightId, seatId));
        verify(seatRepository).findAvailableSeatByFlightIdAndSeatId(testFlightId, seatId, SeatStatus.AVAILABLE);
        verify(seatRepository, never()).save(any(Seat.class));
    }
    
    @Test
    void releaseSeat_ShouldReleaseSeat_WhenSeatExists() {
        // Given
        String seatId = "A1";
        Seat seat = TestDataBuilder.createSeat();
        seat.setSeatId(seatId);
        seat.setStatus(SeatStatus.OCCUPIED);
        
        when(seatRepository.findByFlightIdAndSeatId(testFlightId, seatId))
            .thenReturn(Optional.of(seat));
        when(seatRepository.save(any(Seat.class)))
            .thenReturn(seat);
        
        // When
        Seat result = seatService.releaseSeat(testFlightId, seatId);
        
        // Then
        assertNotNull(result);
        assertEquals(SeatStatus.AVAILABLE, result.getStatus());
        verify(seatRepository).findByFlightIdAndSeatId(testFlightId, seatId);
        verify(seatRepository).save(seat);
    }
    
    @Test
    void releaseSeat_ShouldThrowException_WhenSeatNotFound() {
        // Given
        String seatId = "Z99";
        when(seatRepository.findByFlightIdAndSeatId(testFlightId, seatId))
            .thenReturn(Optional.empty());
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            seatService.releaseSeat(testFlightId, seatId));
        verify(seatRepository).findByFlightIdAndSeatId(testFlightId, seatId);
        verify(seatRepository, never()).save(any(Seat.class));
    }
    
    @Test
    void getSeatsByFlightId_ShouldReturnSeats_WhenSeatsExist() {
        // Given
        when(seatRepository.findByFlightId(testFlightId))
            .thenReturn(testSeats);
        
        // When
        List<Seat> result = seatService.getSeatsByFlightId(testFlightId);
        
        // Then
        assertNotNull(result);
        assertEquals(testSeats.size(), result.size());
        verify(seatRepository).findByFlightId(testFlightId);
    }
    
    @Test
    void getSeatsByFlightId_ShouldReturnEmptyList_WhenNoSeatsExist() {
        // Given
        when(seatRepository.findByFlightId(testFlightId))
            .thenReturn(Arrays.asList());
        
        // When
        List<Seat> result = seatService.getSeatsByFlightId(testFlightId);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(seatRepository).findByFlightId(testFlightId);
    }
    
    @Test
    void isSeatAvailable_ShouldReturnTrue_WhenSeatIsAvailable() {
        // Given
        String seatId = "A1";
        Seat seat = TestDataBuilder.createSeat();
        seat.setSeatId(seatId);
        seat.setStatus(SeatStatus.AVAILABLE);
        
        when(seatRepository.findAvailableSeatByFlightIdAndSeatId(testFlightId, seatId, SeatStatus.AVAILABLE))
            .thenReturn(Optional.of(seat));
        
        // When
        boolean result = seatService.isSeatAvailable(testFlightId, seatId);
        
        // Then
        assertTrue(result);
        verify(seatRepository).findAvailableSeatByFlightIdAndSeatId(testFlightId, seatId, SeatStatus.AVAILABLE);
    }
    
    @Test
    void isSeatAvailable_ShouldReturnFalse_WhenSeatIsNotAvailable() {
        // Given
        String seatId = "A1";
        when(seatRepository.findAvailableSeatByFlightIdAndSeatId(testFlightId, seatId, SeatStatus.AVAILABLE))
            .thenReturn(Optional.empty());
        
        // When
        boolean result = seatService.isSeatAvailable(testFlightId, seatId);
        
        // Then
        assertFalse(result);
        verify(seatRepository).findAvailableSeatByFlightIdAndSeatId(testFlightId, seatId, SeatStatus.AVAILABLE);
    }
}
