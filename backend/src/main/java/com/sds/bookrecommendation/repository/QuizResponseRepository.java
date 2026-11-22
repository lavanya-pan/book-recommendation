package com.sds.bookrecommendation.repository;

import com.sds.bookrecommendation.entity.QuizResponse;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizResponseRepository extends JpaRepository<QuizResponse, Long> {
}