package com.taxi.user.repo;

import com.taxi.common.enums.DriverStatus;
import com.taxi.user.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DriverRepo extends JpaRepository<Driver, Long> {
    List<Driver> findByStatus(DriverStatus status);
}
