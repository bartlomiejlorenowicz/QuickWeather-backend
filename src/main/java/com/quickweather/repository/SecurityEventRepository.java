package com.quickweather.repository;

import com.quickweather.domain.user.SecurityEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityEventRepository extends JpaRepository<SecurityEvent, Long> {
}
