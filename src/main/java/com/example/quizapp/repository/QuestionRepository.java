package com.example.quizapp.repository;

import com.example.quizapp.model.Question;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByQuizIdOrderByIdAsc(Long quizId);
}
