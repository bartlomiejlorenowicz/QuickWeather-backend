package com.quickweather.repository;

import com.quickweather.domain.user.UserActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserActivityRepository extends JpaRepository<UserActivityLog, Long>, JpaSpecificationExecutor<UserActivityLog> {
}
