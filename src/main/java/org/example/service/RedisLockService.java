package org.example.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class RedisLockService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Value("${app.redis.lock-ttl:600000}")
    private long lockTtl;
    
    @Value("${app.redis.seat-lock-prefix:seat:lock:}")
    private String seatLockPrefix;
    
    public String acquireSeatLock(Long flightId, String seatId) {
        String lockKey = seatLockPrefix + flightId + ":" + seatId;
        String lockValue = UUID.randomUUID().toString();
        
        Boolean acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, lockTtl, TimeUnit.MILLISECONDS);
        
        if (Boolean.TRUE.equals(acquired)) {
            return lockValue;
        }
        
        return null;
    }
    
    public boolean releaseSeatLock(Long flightId, String seatId, String lockValue) {
        String lockKey = seatLockPrefix + flightId + ":" + seatId;
        
        // Use Lua script to ensure atomicity
        String luaScript = 
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('del', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end";
        
        Long result = redisTemplate.execute(
            (org.springframework.data.redis.core.RedisCallback<Long>) connection -> 
                connection.eval(luaScript.getBytes(), 
                              org.springframework.data.redis.connection.ReturnType.INTEGER, 
                              1, 
                              lockKey.getBytes(), 
                              lockValue.getBytes())
        );
        
        return result != null && result == 1L;
    }
    
    public boolean isSeatLocked(Long flightId, String seatId) {
        String lockKey = seatLockPrefix + flightId + ":" + seatId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(lockKey));
    }
    
    public boolean extendSeatLock(Long flightId, String seatId, String lockValue) {
        String lockKey = seatLockPrefix + flightId + ":" + seatId;
        
        // Use Lua script to ensure atomicity
        String luaScript = 
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('expire', KEYS[1], ARGV[2]) " +
            "else " +
            "    return 0 " +
            "end";
        
        Long result = redisTemplate.execute(
            (org.springframework.data.redis.core.RedisCallback<Long>) connection -> 
                connection.eval(luaScript.getBytes(), 
                              org.springframework.data.redis.connection.ReturnType.INTEGER, 
                              1, 
                              lockKey.getBytes(), 
                              lockValue.getBytes(),
                              String.valueOf(lockTtl / 1000).getBytes())
        );
        
        return result != null && result == 1L;
    }
}
