package com.taxi.user.service;

import com.taxi.common.enums.DriverStatus;
import com.taxi.user.entity.Driver;
import com.taxi.user.repo.DriverRepo;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DriverCacheService {

    private static final Logger log = LoggerFactory.getLogger(DriverCacheService.class);
    private static final String FREE_DRIVERS_KEY = "drivers:free";

    private final StringRedisTemplate redis;
    private final DriverRepo driverRepo;

    public DriverCacheService(StringRedisTemplate redis, DriverRepo driverRepo) {
        this.redis = redis;
        this.driverRepo = driverRepo;
    }

    @PostConstruct
    public void warmCache() {
        List<Driver> freeDrivers = driverRepo.findByStatus(DriverStatus.FREE);
        if (!freeDrivers.isEmpty()) {
            String[] ids = freeDrivers.stream()
                    .map(d -> d.getId().toString())
                    .toArray(String[]::new);
            redis.opsForSet().add(FREE_DRIVERS_KEY, ids);
            log.info("Redis cache warmed with {} free drivers", ids.length);
        }
    }

    public List<Long> getFreeDriverIds() {
        Set<String> members = redis.opsForSet().members(FREE_DRIVERS_KEY);
        if (members != null && !members.isEmpty()) {
            return members.stream()
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
        }
        // Холодный старт: читаем из БД и греем кэш
        List<Driver> freeDrivers = driverRepo.findByStatus(DriverStatus.FREE);
        if (!freeDrivers.isEmpty()) {
            String[] ids = freeDrivers.stream()
                    .map(d -> d.getId().toString())
                    .toArray(String[]::new);
            redis.opsForSet().add(FREE_DRIVERS_KEY, ids);
        }
        return freeDrivers.stream()
                .map(Driver::getId)
                .collect(Collectors.toList());
    }

    public void markFree(Long driverId) {
        redis.opsForSet().add(FREE_DRIVERS_KEY, driverId.toString());
    }

    public void markBusy(Long driverId) {
        redis.opsForSet().remove(FREE_DRIVERS_KEY, driverId.toString());
    }
}
