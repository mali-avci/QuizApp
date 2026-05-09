package com.example.quizapp.repository;

import com.example.quizapp.model.Quiz;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findAllByOrderByCategoryAscTitleAsc();
}
