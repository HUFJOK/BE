package com.likelion.hufjok.repository;

import com.likelion.hufjok.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}