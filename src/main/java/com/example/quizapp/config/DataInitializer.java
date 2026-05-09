package com.example.quizapp.config;

import com.example.quizapp.model.Choice;
import com.example.quizapp.model.Question;
import com.example.quizapp.model.Quiz;
import com.example.quizapp.model.Role;
import com.example.quizapp.model.User;
import com.example.quizapp.repository.QuizRepository;
import com.example.quizapp.repository.UserRepository;
import java.util.Set;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           QuizRepository quizRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.quizRepository = quizRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setRoles(Set.of(Role.ROLE_ADMIN, Role.ROLE_USER));
            userRepository.save(admin);

            User user = new User();
            user.setUsername("user");
            user.setPasswordHash(passwordEncoder.encode("user123"));
            user.setRoles(Set.of(Role.ROLE_USER));
            userRepository.save(user);
        }

        if (quizRepository.count() == 0) {
            Quiz quiz1 = createQuiz("Java Basics", "Programming", 180);
            addQuestion(quiz1, "Which keyword is used to inherit a class in Java?", 0,
                "extends", "implements", "inherits", "super");
            addQuestion(quiz1, "Which collection does not allow duplicates?", 1,
                "List", "Set", "ArrayList", "Vector");
            addQuestion(quiz1, "Which primitive type stores true/false?", 2,
                "int", "char", "boolean", "double");
            addQuestion(quiz1, "Which method is the entry point in Java?", 3,
                "start()", "run()", "init()", "main()");
            addQuestion(quiz1, "Which keyword prevents inheritance?", 0,
                "final", "static", "private", "this");
            addQuestion(quiz1, "Which operator compares object references?", 1,
                "equals", "==", ".compareTo", "instanceof");
            quizRepository.save(quiz1);

            Quiz quiz2 = createQuiz("SQL Essentials", "Databases", 180);
            addQuestion(quiz2, "Which SQL clause filters rows?", 2,
                "GROUP BY", "ORDER BY", "WHERE", "LIMIT");
            addQuestion(quiz2, "Which command creates a table?", 0,
                "CREATE TABLE", "MAKE TABLE", "NEW TABLE", "ADD TABLE");
            addQuestion(quiz2, "Which keyword sorts results?", 1,
                "FILTER", "ORDER BY", "SORT", "ALIGN");
            addQuestion(quiz2, "Which command removes all rows but keeps table?", 3,
                "DROP", "REMOVE", "DELETE TABLE", "TRUNCATE");
            addQuestion(quiz2, "Which join returns matching rows only?", 0,
                "INNER JOIN", "LEFT JOIN", "RIGHT JOIN", "FULL JOIN");
            addQuestion(quiz2, "Which aggregate counts rows?", 1,
                "SUM", "COUNT", "AVG", "MIN");
            quizRepository.save(quiz2);

            Quiz quiz3 = createQuiz("Web Basics", "Web", 180);
            addQuestion(quiz3, "Which HTTP method is used to fetch data?", 0,
                "GET", "POST", "PUT", "DELETE");
            addQuestion(quiz3, "Which status code means Not Found?", 3,
                "200", "201", "403", "404");
            addQuestion(quiz3, "Which header indicates request body type?", 1,
                "Accept", "Content-Type", "Cache-Control", "Server");
            addQuestion(quiz3, "Which HTML tag creates a link?", 2,
                "<link>", "<nav>", "<a>", "<href>");
            addQuestion(quiz3, "Which CSS property changes text color?", 0,
                "color", "font-style", "background", "border");
            addQuestion(quiz3, "Which HTTP status is Created?", 1,
                "202", "201", "204", "301");
            quizRepository.save(quiz3);

            Quiz quiz4 = createQuiz("Networking", "Networking", 180);
            addQuestion(quiz4, "Which protocol resolves domain names?", 2,
                "HTTP", "FTP", "DNS", "SSH");
            addQuestion(quiz4, "Which port is used by HTTPS by default?", 1,
                "80", "443", "21", "25");
            addQuestion(quiz4, "Which layer does IP belong to?", 0,
                "Network", "Transport", "Application", "Data Link");
            addQuestion(quiz4, "Which protocol guarantees delivery?", 3,
                "UDP", "ICMP", "ARP", "TCP");
            addQuestion(quiz4, "Which device routes packets between networks?", 2,
                "Hub", "Switch", "Router", "Repeater");
            addQuestion(quiz4, "Which address is used in LAN?", 0,
                "Private IP", "Public IP", "Loopback only", "Broadcast only");
            quizRepository.save(quiz4);

            Quiz quiz5 = createQuiz("Software Basics", "General", 180);
            addQuestion(quiz5, "Which practice keeps code changes small and frequent?", 1,
                "Big-bang releases", "Continuous Integration", "Waterfall", "Freeze" );
            addQuestion(quiz5, "Which versioning scheme uses MAJOR.MINOR.PATCH?", 0,
                "Semantic Versioning", "Calendar Versioning", "Binary Versioning", "Random");
            addQuestion(quiz5, "Which tool tracks source code history?", 2,
                "Docker", "Maven", "Git", "Jenkins");
            addQuestion(quiz5, "Which diagram shows classes and relationships?", 3,
                "Flowchart", "Sequence Diagram", "ER Diagram", "Class Diagram");
            addQuestion(quiz5, "Which testing level checks a single method?", 0,
                "Unit Test", "System Test", "Load Test", "Acceptance Test");
            addQuestion(quiz5, "Which file lists Maven dependencies?", 1,
                "build.gradle", "pom.xml", "package.json", "composer.json");
            quizRepository.save(quiz5);
        }
    }

    private Quiz createQuiz(String title, String category, int durationSeconds) {
        Quiz quiz = new Quiz();
        quiz.setTitle(title);
        quiz.setCategory(category);
        quiz.setDurationSeconds(durationSeconds);
        return quiz;
    }

    private void addQuestion(Quiz quiz, String text, int correctIndex, String... choices) {
        Question question = new Question();
        question.setQuiz(quiz);
        question.setText(text);

        for (int i = 0; i < choices.length; i++) {
            Choice choice = new Choice();
            choice.setQuestion(question);
            choice.setText(choices[i]);
            choice.setCorrect(i == correctIndex);
            question.getChoices().add(choice);
        }

        quiz.getQuestions().add(question);
    }
}
