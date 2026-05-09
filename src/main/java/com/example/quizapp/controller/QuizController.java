package com.example.quizapp.controller;

import com.example.quizapp.dto.AnswerReviewItem;
import com.example.quizapp.model.AttemptAnswer;
import com.example.quizapp.model.Choice;
import com.example.quizapp.model.Quiz;
import com.example.quizapp.model.QuizAttempt;
import com.example.quizapp.model.User;
import com.example.quizapp.repository.QuizAttemptRepository;
import com.example.quizapp.repository.UserRepository;
import com.example.quizapp.service.AttemptService;
import com.example.quizapp.service.QuizService;
import jakarta.servlet.http.HttpSession;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class QuizController {
    private final QuizService quizService;
    private final AttemptService attemptService;
    private final QuizAttemptRepository attemptRepository;
    private final UserRepository userRepository;

    public QuizController(QuizService quizService,
                          AttemptService attemptService,
                          QuizAttemptRepository attemptRepository,
                          UserRepository userRepository) {
        this.quizService = quizService;
        this.attemptService = attemptService;
        this.attemptRepository = attemptRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/quizzes";
    }

    @GetMapping("/quizzes")
    public String list(Model model) {
        model.addAttribute("quizzes", quizService.listQuizzes());
        return "quizzes/list";
    }

    @GetMapping("/quizzes/{id}")
    public String takeQuiz(@PathVariable Long id, Authentication authentication, Model model, HttpSession session) {
        Quiz quiz = quizService.getQuiz(id);
        User user = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        QuizAttempt attempt = attemptService.startAttempt(user, quiz);
        session.setAttribute("attemptId", attempt.getId());

        model.addAttribute("quiz", quiz);
        model.addAttribute("attempt", attempt);
        return "quizzes/take";
    }

    @PostMapping("/quizzes/{id}/submit")
    public String submitQuiz(@PathVariable Long id,
                             @RequestParam Map<String, String> params,
                             HttpSession session) {
        Object attemptIdObj = session.getAttribute("attemptId");
        if (attemptIdObj == null) {
            return "redirect:/quizzes";
        }
        Long attemptId = (Long) attemptIdObj;
        session.removeAttribute("attemptId");

        Map<Long, Long> answers = new HashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getKey().startsWith("q_")) {
                Long questionId = Long.valueOf(entry.getKey().substring(2));
                Long choiceId = Long.valueOf(entry.getValue());
                answers.put(questionId, choiceId);
            }
        }
        QuizAttempt attempt = attemptRepository.findById(attemptId)
            .orElseThrow(() -> new IllegalArgumentException("Attempt not found"));
        LocalDateTime startedAt = attempt.getStartedAt();
        int durationSeconds = attempt.getDurationSeconds();
        long elapsedSeconds = Duration.between(startedAt, LocalDateTime.now()).getSeconds();
        if (elapsedSeconds > durationSeconds) {
            // Time is up, still record answers but keep finish time.
        }
        attemptService.submitAttempt(attemptId, answers);
        return "redirect:/attempts/" + attemptId;
    }

    @GetMapping("/attempts/{id}")
    public String result(@PathVariable Long id, Model model) {
        QuizAttempt attempt = attemptRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Attempt not found"));
        long correct = attempt.getAnswers().stream().filter(a -> a.isCorrect()).count();
        long wrong = attempt.getAnswers().size() - correct;
        int total = attempt.getAnswers().isEmpty() ? 1 : attempt.getAnswers().size();
        int correctPercent = (int) Math.round((correct * 100.0) / total);
        int wrongPercent = 100 - correctPercent;
        List<AnswerReviewItem> reviews = buildReviews(attempt.getAnswers());
        model.addAttribute("attempt", attempt);
        model.addAttribute("correctCount", correct);
        model.addAttribute("wrongCount", wrong);
        model.addAttribute("correctPercent", correctPercent);
        model.addAttribute("wrongPercent", wrongPercent);
        model.addAttribute("reviews", reviews);
        return "quizzes/result";
    }

    private List<AnswerReviewItem> buildReviews(List<AttemptAnswer> answers) {
        List<AnswerReviewItem> reviews = new ArrayList<>();
        for (AttemptAnswer answer : answers) {
            String selectedText = "No answer";
            String correctText = "";
            Long selectedId = answer.getSelectedChoiceId();
            for (Choice choice : answer.getQuestion().getChoices()) {
                if (choice.isCorrect()) {
                    correctText = choice.getText();
                }
                if (selectedId != null && selectedId.equals(choice.getId())) {
                    selectedText = choice.getText();
                }
            }
            reviews.add(new AnswerReviewItem(
                answer.getQuestion().getText(),
                selectedText,
                correctText,
                answer.isCorrect()
            ));
        }
        return reviews;
    }

    @GetMapping("/history")
    public String history(Authentication authentication, Model model) {
        User user = userRepository.findByUsername(authentication.getName())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        List<QuizAttempt> attempts = attemptRepository.findByUserAndFinishedAtIsNotNullOrderByFinishedAtDesc(user);
        model.addAttribute("attempts", attempts);
        return "quizzes/history";
    }

    @GetMapping("/leaderboard")
    public String leaderboard(Model model) {
        model.addAttribute("attempts", attemptRepository.findTop10ByFinishedAtIsNotNullOrderByScoreDescFinishedAtAsc());
        return "quizzes/leaderboard";
    }
}
