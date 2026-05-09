package com.taxi.user.controller;

import com.taxi.user.dto.UpdateDriverStatusRequest;
import com.taxi.user.entity.Driver;
import com.taxi.user.repo.DriverRepo;
import com.taxi.user.service.DriverCacheService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/drivers")
public class DriverController {

    private final DriverRepo driverRepo;
    private final DriverCacheService driverCacheService;

    public DriverController(DriverRepo driverRepo, DriverCacheService driverCacheService) {
        this.driverRepo = driverRepo;
        this.driverCacheService = driverCacheService;
    }

    @GetMapping("/free")
    public ResponseEntity<List<Long>> getFreeDrivers() {
        return ResponseEntity.ok(driverCacheService.getFreeDriverIds());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Driver> getDriver(@PathVariable Long id) {
        return driverRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                          @RequestBody UpdateDriverStatusRequest req) {
        Driver driver = driverRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Водитель не найден"));

        driver.setStatus(req.getStatus());
        Driver saved = driverRepo.save(driver);

        // Синхронизация Redis-кэша
        switch (req.getStatus()) {
            case FREE -> driverCacheService.markFree(id);
            case BUSY, OFFLINE -> driverCacheService.markBusy(id);
        }

        return ResponseEntity.ok(saved);
    }
}
