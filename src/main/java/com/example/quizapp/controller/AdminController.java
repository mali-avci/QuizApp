package com.example.quizapp.controller;

import com.example.quizapp.dto.QuestionForm;
import com.example.quizapp.model.Choice;
import com.example.quizapp.model.Question;
import com.example.quizapp.model.Quiz;
import com.example.quizapp.repository.QuestionRepository;
import com.example.quizapp.repository.QuizAttemptRepository;
import com.example.quizapp.repository.QuizRepository;
import com.example.quizapp.service.QuizService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final QuizService quizService;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuizAttemptRepository attemptRepository;

    public AdminController(QuizService quizService,
                           QuizRepository quizRepository,
                           QuestionRepository questionRepository,
                           QuizAttemptRepository attemptRepository) {
        this.quizService = quizService;
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.attemptRepository = attemptRepository;
    }

    @GetMapping("/quizzes")
    public String quizzes(Model model) {
        model.addAttribute("quizzes", quizService.listQuizzes());
        return "admin/quizzes";
    }

    @GetMapping("/quizzes/new")
    public String newQuiz(Model model) {
        model.addAttribute("quiz", new Quiz());
        return "admin/quiz-form";
    }

    @PostMapping("/quizzes")
    public String createQuiz(@Valid Quiz quiz, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin/quiz-form";
        }
        quizService.save(quiz);
        return "redirect:/admin/quizzes";
    }

    @GetMapping("/quizzes/{id}/edit")
    public String editQuiz(@PathVariable Long id, Model model) {
        model.addAttribute("quiz", quizService.getQuiz(id));
        return "admin/quiz-form";
    }

    @PostMapping("/quizzes/{id}")
    public String updateQuiz(@PathVariable Long id, @Valid Quiz quiz, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin/quiz-form";
        }
        quiz.setId(id);
        quizService.save(quiz);
        return "redirect:/admin/quizzes";
    }

    @GetMapping("/quizzes/{id}/questions")
    public String questions(@PathVariable Long id, Model model) {
        model.addAttribute("quiz", quizService.getQuiz(id));
        model.addAttribute("questions", questionRepository.findByQuizIdOrderByIdAsc(id));
        return "admin/questions";
    }

    @GetMapping("/quizzes/{id}/questions/new")
    public String newQuestion(@PathVariable Long id, Model model) {
        model.addAttribute("quiz", quizService.getQuiz(id));
        model.addAttribute("form", new QuestionForm());
        return "admin/question-form";
    }

    @PostMapping("/quizzes/{id}/questions")
    public String createQuestion(@PathVariable Long id,
                                 @Valid QuestionForm form,
                                 BindingResult bindingResult,
                                 Model model) {
        Quiz quiz = quizService.getQuiz(id);
        if (bindingResult.hasErrors()) {
            model.addAttribute("quiz", quiz);
            return "admin/question-form";
        }

        Question question = buildQuestionFromForm(form, quiz, null);
        questionRepository.save(question);
        return "redirect:/admin/quizzes/" + id + "/questions";
    }

    @GetMapping("/questions/{id}/edit")
    public String editQuestion(@PathVariable Long id, Model model) {
        Question question = questionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        model.addAttribute("quiz", question.getQuiz());
        model.addAttribute("form", toForm(question));
        model.addAttribute("questionId", id);
        return "admin/question-form";
    }

    @PostMapping("/questions/{id}")
    public String updateQuestion(@PathVariable Long id,
                                 @Valid QuestionForm form,
                                 BindingResult bindingResult,
                                 Model model) {
        Question question = questionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        if (bindingResult.hasErrors()) {
            model.addAttribute("quiz", question.getQuiz());
            model.addAttribute("questionId", id);
            return "admin/question-form";
        }
        Question updated = buildQuestionFromForm(form, question.getQuiz(), question);
        updated.setId(id);
        questionRepository.save(updated);
        return "redirect:/admin/quizzes/" + question.getQuiz().getId() + "/questions";
    }

    @PostMapping("/questions/{id}/delete")
    public String deleteQuestion(@PathVariable Long id) {
        Question question = questionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        Long quizId = question.getQuiz().getId();
        questionRepository.delete(question);
        return "redirect:/admin/quizzes/" + quizId + "/questions";
    }

    @GetMapping("/results")
    public String results(Model model) {
        model.addAttribute("attempts", attemptRepository.findAllByFinishedAtIsNotNullOrderByFinishedAtDesc());
        return "admin/users-results";
    }

    private Question buildQuestionFromForm(QuestionForm form, Quiz quiz, Question existing) {
        Question question = existing == null ? new Question() : existing;
        question.setQuiz(quiz);
        question.setText(form.getText());
        question.getChoices().clear();
        List<String> choices = List.of(form.getChoiceA(), form.getChoiceB(), form.getChoiceC(), form.getChoiceD());
        for (int i = 0; i < choices.size(); i++) {
            Choice choice = new Choice();
            choice.setQuestion(question);
            choice.setText(choices.get(i));
            choice.setCorrect(i == form.getCorrectIndex());
            question.getChoices().add(choice);
        }
        return question;
    }

    private QuestionForm toForm(Question question) {
        QuestionForm form = new QuestionForm();
        form.setText(question.getText());
        List<Choice> choices = question.getChoices();
        form.setChoiceA(choices.size() > 0 ? choices.get(0).getText() : "");
        form.setChoiceB(choices.size() > 1 ? choices.get(1).getText() : "");
        form.setChoiceC(choices.size() > 2 ? choices.get(2).getText() : "");
        form.setChoiceD(choices.size() > 3 ? choices.get(3).getText() : "");
        int correctIndex = 0;
        for (int i = 0; i < choices.size(); i++) {
            if (choices.get(i).isCorrect()) {
                correctIndex = i;
                break;
            }
        }
        form.setCorrectIndex(correctIndex);
        return form;
    }
}
