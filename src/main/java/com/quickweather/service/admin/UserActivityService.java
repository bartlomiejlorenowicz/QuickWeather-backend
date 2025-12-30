package com.quickweather.service.admin;

import com.quickweather.domain.user.UserActivityLog;
import com.quickweather.repository.UserActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class UserActivityService {

    private final UserActivityRepository userActivityRepository;

    public UserActivityLog logActivity(String userId, String email, String city, String activity) {
        UserActivityLog log = new UserActivityLog(userId, email, city, LocalDateTime.now(), activity);
        return userActivityRepository.save(log);
    }

    public List<UserActivityLog> search(String userId, String email, String city, LocalDate startDate, LocalDate endDate) {
        Specification<UserActivityLog> spec = Specification.where(null);

        if (userId != null && !userId.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("userId"), userId));
        }
        if (email != null && !email.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("email"), email));
        }
        if (city != null && !city.isEmpty()) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("city"), city));
        }
        if (startDate != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("timestamp"), startDate.atStartOfDay()));
        }
        if (endDate != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("timestamp"), endDate.atTime(23, 59, 59)));
        }

        return userActivityRepository.findAll(spec);
    }

}
