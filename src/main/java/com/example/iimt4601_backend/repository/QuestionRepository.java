package com.example.iimt4601_backend.repository;

import com.example.iimt4601_backend.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    Page<Question> findByProductId(Long productId, Pageable pageable);

    int countByProductId(Long productId);
    Page<Question> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

}