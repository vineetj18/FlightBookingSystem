package org.example.service;

import org.example.model.Seat;
import org.example.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class SeatCreationService {
    
    @Autowired
    private SeatRepository seatRepository;
    
    public void createSeatsForFlight(Long flightId, Integer maxPassengers) {
        List<Seat> seats = new ArrayList<>();
        
        // Create seats in rows (A, B, C, etc.) and columns (1, 2, 3, etc.)
        int rows = (maxPassengers + 5) / 6; // 6 seats per row
        int seatNumber = 1;
        
        for (int row = 0; row < rows; row++) {
            char rowLetter = (char) ('A' + row);
            for (int col = 1; col <= 6 && seatNumber <= maxPassengers; col++) {
                String seatId = String.valueOf(rowLetter) + col;
                Seat seat = new Seat(flightId, seatId);
                seats.add(seat);
                seatNumber++;
            }
        }
        
        seatRepository.saveAll(seats);
    }
}
