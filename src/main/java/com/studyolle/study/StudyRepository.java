package com.studyolle.study;


import com.studyolle.domain.Study;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyRepository extends JpaRepository<Study, Long>{

    boolean existsByPath(String path);
}
