package org.example.service;

public interface LockService {
    String acquireSeatLock(Long flightId, String seatId);
    boolean releaseSeatLock(Long flightId, String seatId, String lockValue);
    boolean isSeatLocked(Long flightId, String seatId);
    boolean extendSeatLock(Long flightId, String seatId, String lockValue);
}

