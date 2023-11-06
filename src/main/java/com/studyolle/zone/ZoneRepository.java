package com.studyolle.zone;

import com.studyolle.domain.Zone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ZoneRepository extends JpaRepository<Zone, Long> {
    Zone findByCityAndProvince(String cityName, String provinceName);
}
