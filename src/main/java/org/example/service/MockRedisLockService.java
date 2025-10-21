package org.example.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.redis.enabled", havingValue = "false", matchIfMissing = true)
public class MockRedisLockService implements LockService {
    
    public String acquireSeatLock(Long flightId, String seatId) {
        // Mock implementation - always return a lock value
        return UUID.randomUUID().toString();
    }
    
    public boolean releaseSeatLock(Long flightId, String seatId, String lockValue) {
        // Mock implementation - always return true
        return true;
    }
    
    public boolean isSeatLocked(Long flightId, String seatId) {
        // Mock implementation - always return false (not locked)
        return false;
    }
    
    public boolean extendSeatLock(Long flightId, String seatId, String lockValue) {
        // Mock implementation - always return true
        return true;
    }
}
