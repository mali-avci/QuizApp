package com.example.quizapp.service;

import com.example.quizapp.model.Quiz;
import com.example.quizapp.repository.QuizRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class QuizService {
    private final QuizRepository quizRepository;

    public QuizService(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    public List<Quiz> listQuizzes() {
        return quizRepository.findAllByOrderByCategoryAscTitleAsc();
    }

    public Quiz getQuiz(Long id) {
        return quizRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
    }

    public Quiz save(Quiz quiz) {
        return quizRepository.save(quiz);
    }
}
