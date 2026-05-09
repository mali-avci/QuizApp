package com.example.quizapp.service;

import com.example.quizapp.model.AttemptAnswer;
import com.example.quizapp.model.Choice;
import com.example.quizapp.model.Question;
import com.example.quizapp.model.Quiz;
import com.example.quizapp.model.QuizAttempt;
import com.example.quizapp.model.User;
import com.example.quizapp.repository.QuestionRepository;
import com.example.quizapp.repository.QuizAttemptRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttemptService {
    private final QuizAttemptRepository attemptRepository;
    private final QuestionRepository questionRepository;

    public AttemptService(QuizAttemptRepository attemptRepository, QuestionRepository questionRepository) {
        this.attemptRepository = attemptRepository;
        this.questionRepository = questionRepository;
    }

    public QuizAttempt startAttempt(User user, Quiz quiz) {
        QuizAttempt attempt = new QuizAttempt();
        attempt.setUser(user);
        attempt.setQuiz(quiz);
        attempt.setStartedAt(LocalDateTime.now());
        attempt.setTotal(quiz.getQuestions().size());
        attempt.setScore(0);
        attempt.setDurationSeconds(quiz.getDurationSeconds());
        return attemptRepository.save(attempt);
    }

    @Transactional
    public QuizAttempt submitAttempt(Long attemptId, Map<Long, Long> answersByQuestion) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
            .orElseThrow(() -> new IllegalArgumentException("Attempt not found"));

        List<Question> questions = questionRepository.findByQuizIdOrderByIdAsc(attempt.getQuiz().getId());
        attempt.getAnswers().clear();
        attempt.setTotal(questions.size());
        int score = 0;
        for (Question question : questions) {
            Long selectedChoiceId = answersByQuestion.get(question.getId());
            boolean correct = false;
            if (selectedChoiceId != null) {
                for (Choice choice : question.getChoices()) {
                    if (choice.getId().equals(selectedChoiceId)) {
                        correct = choice.isCorrect();
                        break;
                    }
                }
            }
            if (correct) {
                score += 1;
            }
            AttemptAnswer answer = new AttemptAnswer();
            answer.setAttempt(attempt);
            answer.setQuestion(question);
            answer.setSelectedChoiceId(selectedChoiceId == null ? -1L : selectedChoiceId);
            answer.setCorrect(correct);
            attempt.getAnswers().add(answer);
        }

        attempt.setScore(score);
        attempt.setFinishedAt(LocalDateTime.now());
        return attemptRepository.save(attempt);
    }
}
