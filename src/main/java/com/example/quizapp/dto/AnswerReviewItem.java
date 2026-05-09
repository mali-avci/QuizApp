package com.example.quizapp.dto;

public class AnswerReviewItem {
    private final String questionText;
    private final String selectedText;
    private final String correctText;
    private final boolean correct;

    public AnswerReviewItem(String questionText, String selectedText, String correctText, boolean correct) {
        this.questionText = questionText;
        this.selectedText = selectedText;
        this.correctText = correctText;
        this.correct = correct;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String getSelectedText() {
        return selectedText;
    }

    public String getCorrectText() {
        return correctText;
    }

    public boolean isCorrect() {
        return correct;
    }
}
