package com.example.commute.repository;

import com.example.commute.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface  UserRepository  extends JpaRepository<User, Long> {
}
