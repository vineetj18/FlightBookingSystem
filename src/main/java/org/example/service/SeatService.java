package org.example.service;

import org.example.model.Seat;
import org.example.repository.SeatRepository;
import org.example.enums.SeatStatus;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class SeatService {
    
    @Autowired
    private SeatRepository seatRepository;
    
    @RabbitListener(queues = "${app.queue.seat-creation-queue:seat.creation.queue}")
    public void createSeatsForFlight(FlightService.SeatCreationMessage message) {
        Long flightId = message.getFlightId();
        Integer maxPassengers = message.getMaxPassengers();
        
        // Create seats for the flight
        for (int i = 1; i <= maxPassengers; i++) {
            String seatId = generateSeatId(i);
            Seat seat = new Seat(flightId, seatId);
            seatRepository.save(seat);
        }
    }
    
    public List<Seat> getAvailableSeatsByFlightId(Long flightId) {
        return seatRepository.findAvailableSeatsByFlightId(flightId, SeatStatus.AVAILABLE);
    }
    
    public List<Seat> getSeatsByFlightId(Long flightId) {
        return seatRepository.findByFlightId(flightId);
    }
    
    public Optional<Seat> getSeatByFlightIdAndSeatId(Long flightId, String seatId) {
        return seatRepository.findByFlightIdAndSeatId(flightId, seatId);
    }
    
    public boolean isSeatAvailable(Long flightId, String seatId) {
        Optional<Seat> seat = seatRepository.findAvailableSeatByFlightIdAndSeatId(
            flightId, seatId, SeatStatus.AVAILABLE);
        return seat.isPresent();
    }
    
    public Seat lockSeat(Long flightId, String seatId) {
        Optional<Seat> seatOpt = seatRepository.findAvailableSeatByFlightIdAndSeatId(
            flightId, seatId, SeatStatus.AVAILABLE);
        
        if (seatOpt.isEmpty()) {
            throw new IllegalArgumentException("Seat " + seatId + " is not available for flight " + flightId);
        }
        
        Seat seat = seatOpt.get();
        seat.lock();
        return seatRepository.save(seat);
    }
    
    public Seat occupySeat(Long flightId, String seatId) {
        Optional<Seat> seatOpt = seatRepository.findByFlightIdAndSeatId(flightId, seatId);
        
        if (seatOpt.isEmpty()) {
            throw new IllegalArgumentException("Seat " + seatId + " not found for flight " + flightId);
        }
        
        Seat seat = seatOpt.get();
        seat.occupy();
        return seatRepository.save(seat);
    }
    
    public Seat releaseSeat(Long flightId, String seatId) {
        Optional<Seat> seatOpt = seatRepository.findByFlightIdAndSeatId(flightId, seatId);
        
        if (seatOpt.isEmpty()) {
            throw new IllegalArgumentException("Seat " + seatId + " not found for flight " + flightId);
        }
        
        Seat seat = seatOpt.get();
        seat.release();
        return seatRepository.save(seat);
    }
    
    public Long getAvailableSeatsCount(Long flightId) {
        return seatRepository.countAvailableSeatsByFlightId(flightId, SeatStatus.AVAILABLE);
    }
    
    public List<Seat> getAvailableSeatsSequentially(Long flightId, Integer numberOfPassengers) {
        List<Seat> availableSeats = seatRepository.findAvailableSeatsByFlightId(flightId, SeatStatus.AVAILABLE)
                .stream()
                .sorted((s1, s2) -> s1.getSeatId().compareTo(s2.getSeatId()))
                .collect(Collectors.toList());
        
        if (availableSeats.size() < numberOfPassengers) {
            throw new SeatNotAvailableException("Not enough seats available for flight " + flightId);
        }
        
        return availableSeats.subList(0, numberOfPassengers);
    }
    
    private String generateSeatId(int seatNumber) {
        // Generate seat ID like A1, A2, B1, B2, etc.
        int row = (seatNumber - 1) / 6; // 6 seats per row
        int col = ((seatNumber - 1) % 6) + 1;
        char rowChar = (char) ('A' + row);
        return rowChar + String.valueOf(col);
    }
}
