package com.taxi.user.service;

import com.taxi.common.dto.LoginRequest;
import com.taxi.common.dto.RegisterRequest;
import com.taxi.common.enums.UserType;
import com.taxi.user.entity.Driver;
import com.taxi.user.entity.Passenger;
import com.taxi.user.entity.User;
import com.taxi.user.repo.DriverRepo;
import com.taxi.user.repo.PassengerRepo;
import com.taxi.user.repo.UserRepo;
import com.taxi.user.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepo userRepo;
    private final PassengerRepo passengerRepo;
    private final DriverRepo driverRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepo userRepo, PassengerRepo passengerRepo,
                       DriverRepo driverRepo, PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.passengerRepo = passengerRepo;
        this.driverRepo = driverRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public User register(RegisterRequest req) {
        if (userRepo.findByEmail(req.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email уже занят");
        }

        if (req.getUserType() == UserType.PASSENGER) {
            Passenger p = passengerRepo.save(
                    Passenger.builder()
                            .name(req.getName())
                            .phone(req.getPhone())
                            .build()
            );
            User u = User.builder()
                    .email(req.getEmail())
                    .password(passwordEncoder.encode(req.getPassword()))
                    .userType(UserType.PASSENGER)
                    .refId(p.getId())
                    .build();
            return userRepo.save(u);
        } else {
            Driver d = driverRepo.save(
                    Driver.builder()
                            .name(req.getName())
                            .phone(req.getPhone())
                            .build()
            );
            User u = User.builder()
                    .email(req.getEmail())
                    .password(passwordEncoder.encode(req.getPassword()))
                    .userType(UserType.DRIVER)
                    .refId(d.getId())
                    .build();
            return userRepo.save(u);
        }
    }

    public String login(LoginRequest req) {
        User user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Неверный email или пароль"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Неверный email или пароль");
        }

        return jwtUtil.generateToken(user);
    }
}
