package com.example.commute.repository;

import com.example.commute.entity.Photo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
    // 필요시, 추가적인 쿼리 메서드들을 정의할 수 있습니다.
}
