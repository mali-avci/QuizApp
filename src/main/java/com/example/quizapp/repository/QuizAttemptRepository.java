package com.example.quizapp.repository;

import com.example.quizapp.model.QuizAttempt;
import com.example.quizapp.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByUserAndFinishedAtIsNotNullOrderByFinishedAtDesc(User user);

    List<QuizAttempt> findTop10ByFinishedAtIsNotNullOrderByScoreDescFinishedAtAsc();

    List<QuizAttempt> findAllByFinishedAtIsNotNullOrderByFinishedAtDesc();
}
