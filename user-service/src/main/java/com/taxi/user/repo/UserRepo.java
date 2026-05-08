package com.taxi.user.repo;

import com.taxi.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email); //опшионал удобная штука чтоб не ловить ошибку пустоты
}
