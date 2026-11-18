package com.marabagam.quiz.service;

import com.marabagam.certificate.service.CertificateService;
import com.marabagam.common.exception.ForbiddenActionException;
import com.marabagam.common.exception.InvalidInputException;
import com.marabagam.common.exception.ResourceNotFoundException;
import com.marabagam.gamification.domain.SourceActivity;
import com.marabagam.gamification.service.GamificationService;
import com.marabagam.learning.domain.Course;
import com.marabagam.learning.domain.CourseModule;
import com.marabagam.learning.domain.Lesson;
import com.marabagam.learning.domain.Media;
import com.marabagam.learning.repository.CourseModuleRepository;
import com.marabagam.learning.repository.CourseRepository;
import com.marabagam.learning.repository.LessonRepository;
import com.marabagam.learning.repository.MediaRepository;
import com.marabagam.quiz.domain.*;
import com.marabagam.quiz.dto.*;
import com.marabagam.quiz.repository.*;
import com.marabagam.user.domain.Role;
import com.marabagam.user.domain.User;
import com.marabagam.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private static final Logger log = LoggerFactory.getLogger(QuizService.class);

    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuizQuestionRepository quizQuestionRepository;

    @Autowired
    private QuestionOptionRepository questionOptionRepository;

    @Autowired
    private QuizAttemptRepository quizAttemptRepository;

    @Autowired
    private QuizAttemptAnswerRepository quizAttemptAnswerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private CourseModuleRepository courseModuleRepository;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private GamificationService gamificationService;

    @Autowired
    private CertificateService certificateService;

    @Transactional(readOnly = true)
    public QuizDto getQuizForLearner(UUID quizId) {
        Quiz quiz = quizRepository.findByIdAndStatus(quizId, QuizStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Published quiz not found with ID: " + quizId));

        List<QuizQuestion> questions = quizQuestionRepository.findByQuizIdOrderByDisplayOrderAsc(quiz.getId());
        List<QuizQuestionDto> questionDtos = new ArrayList<>();

        for (QuizQuestion q : questions) {
            QuizQuestionDto qDto = new QuizQuestionDto();
            qDto.setId(q.getId());
            qDto.setQuestionText(q.getQuestionText());
            qDto.setQuestionTextTa(q.getQuestionTextTa());
            qDto.setQuestionType(q.getQuestionType());
            qDto.setPoints(q.getPoints());
            qDto.setDisplayOrder(q.getDisplayOrder());

            if (q.getMedia() != null) {
                qDto.setMediaId(q.getMedia().getId());
                qDto.setMediaUrl(q.getMedia().getUrl());
            }

            List<QuestionOption> options = questionOptionRepository.findByQuestionIdOrderByDisplayOrderAsc(q.getId());
            List<QuestionOptionDto> optionDtos = options.stream().map(o -> {
                QuestionOptionDto oDto = new QuestionOptionDto();
                oDto.setId(o.getId());
                oDto.setOptionText(o.getOptionText());
                oDto.setOptionTextTa(o.getOptionTextTa());
                oDto.setDisplayOrder(o.getDisplayOrder());
                // EXCLUDE isCorrect and explanations during active quiz attempt!
                oDto.setIsCorrect(null);
                oDto.setExplanationEn(null);
                oDto.setExplanationTa(null);
                return oDto;
            }).collect(Collectors.toList());

            qDto.setOptions(optionDtos);
            questionDtos.add(qDto);
        }

        QuizDto dto = convertToDto(quiz);
        dto.setQuestions(questionDtos);
        return dto;
    }

    @Transactional
    public AttemptResultDto startQuizAttempt(UUID quizId, User user) {
        Quiz quiz = quizRepository.findByIdAndStatus(quizId, QuizStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Published quiz not found with ID: " + quizId));

        // Check active in-progress attempt
        Optional<QuizAttempt> inProgressOpt = quizAttemptRepository.findFirstByQuizIdAndUserIdAndStatus(
                quiz.getId(), user.getId(), AttemptStatus.STARTED
        );
        if (inProgressOpt.isPresent()) {
            return convertAttemptToResult(inProgressOpt.get(), false);
        }

        // Count completed attempts
        int attemptCount = quizAttemptRepository.countByQuizIdAndUserIdAndStatusNot(
                quiz.getId(), user.getId(), AttemptStatus.STARTED
        );

        if (attemptCount >= quiz.getMaxAttempts()) {
            throw new ForbiddenActionException("Maximum attempt limit (" + quiz.getMaxAttempts() + ") reached for this quiz.");
        }

        QuizAttempt attempt = new QuizAttempt();
        attempt.setQuiz(quiz);
        attempt.setUser(user);
        attempt.setAttemptNumber(attemptCount + 1);
        attempt.setStatus(AttemptStatus.STARTED);
        attempt.setStartedAt(Instant.now());

        QuizAttempt saved = quizAttemptRepository.save(attempt);
        log.info("Started quiz attempt {} for user {} on quiz {}", saved.getAttemptNumber(), user.getId(), quiz.getId());

        return convertAttemptToResult(saved, false);
    }

    @Transactional
    public AttemptResultDto submitQuizAttempt(UUID attemptId, QuizSubmissionDto submission, User user) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz attempt not found: " + attemptId));

        if (!attempt.getUser().getId().equals(user.getId())) {
            throw new ForbiddenActionException("Access denied: Cannot submit attempt belonging to another user");
        }

        if (attempt.getStatus() != AttemptStatus.STARTED) {
            // Already submitted or finalized
            return convertAttemptToResult(attempt, true);
        }

        Quiz quiz = attempt.getQuiz();
        Instant now = Instant.now();

        // Check time limit + 30-second grace period
        if (quiz.getTimeLimitMinutes() != null && quiz.getTimeLimitMinutes() > 0) {
            long secondsAllowed = (quiz.getTimeLimitMinutes() * 60L) + 30L; // 30s grace
            long secondsElapsed = Duration.between(attempt.getStartedAt(), now).getSeconds();

            if (secondsElapsed > secondsAllowed) {
                attempt.setStatus(AttemptStatus.TIMED_OUT);
                attempt.setSubmittedAt(now);
                attempt.setScoreEarned(0.0);
                attempt.setPercentage(0.0);
                attempt.setPassed(false);
                quizAttemptRepository.save(attempt);
                log.warn("Quiz attempt {} timed out for user {}", attemptId, user.getId());
                return convertAttemptToResult(attempt, true);
            }
        }

        List<QuizQuestion> questions = quizQuestionRepository.findByQuizIdOrderByDisplayOrderAsc(quiz.getId());
        Map<UUID, QuizQuestion> questionMap = questions.stream().collect(Collectors.toMap(QuizQuestion::getId, q -> q));

        double earnedPoints = 0.0;
        double totalPoints = questions.stream().mapToDouble(QuizQuestion::getPoints).sum();

        List<SubmitAnswerDto> submittedAnswers = submission != null && submission.getAnswers() != null ? submission.getAnswers() : Collections.emptyList();

        for (SubmitAnswerDto answerDto : submittedAnswers) {
            if (answerDto.getQuestionId() == null) continue;

            QuizQuestion question = questionMap.get(answerDto.getQuestionId());
            if (question == null) {
                throw new InvalidInputException("Submitted question ID does not belong to this quiz.");
            }

            QuestionOption selectedOption = null;
            boolean isCorrect = false;
            int pointsAwarded = 0;

            if (answerDto.getSelectedOptionId() != null) {
                selectedOption = questionOptionRepository.findById(answerDto.getSelectedOptionId())
                        .orElseThrow(() -> new ResourceNotFoundException("Option not found: " + answerDto.getSelectedOptionId()));

                // Validate option belongs to question
                if (!selectedOption.getQuestion().getId().equals(question.getId())) {
                    throw new InvalidInputException("Selected option does not belong to question.");
                }

                if (Boolean.TRUE.equals(selectedOption.getCorrect())) {
                    isCorrect = true;
                    pointsAwarded = question.getPoints();
                    earnedPoints += pointsAwarded;
                }
            }

            QuizAttemptAnswer qAnswer = new QuizAttemptAnswer();
            qAnswer.setAttempt(attempt);
            qAnswer.setQuestion(question);
            qAnswer.setSelectedOption(selectedOption);
            qAnswer.setCorrect(isCorrect);
            qAnswer.setPointsAwarded(pointsAwarded);
            quizAttemptAnswerRepository.save(qAnswer);
        }

        double percentage = totalPoints > 0 ? (earnedPoints / totalPoints) * 100.0 : 0.0;
        boolean passed = percentage >= quiz.getPassPercentage();

        attempt.setScoreEarned(earnedPoints);
        attempt.setTotalPossibleScore(totalPoints);
        attempt.setPercentage(percentage);
        attempt.setPassed(passed);
        attempt.setStatus(AttemptStatus.SUBMITTED);
        attempt.setSubmittedAt(now);

        quizAttemptRepository.save(attempt);
        log.info("Submitted quiz attempt {} for user {}: score={}/{} ({}%), passed={}", attemptId, user.getId(), earnedPoints, totalPoints, percentage, passed);

        int xpAwarded = 0;
        if (passed) {
            boolean isFirstPass = attempt.getAttemptNumber() == 1;
            int baseScoreXp = isFirstPass ? 50 : 25;
            String passKey = isFirstPass ? "QUIZ_PASS_FIRST:" + user.getId() + ":" + quiz.getId() : "QUIZ_PASS_RETAKE:" + user.getId() + ":" + quiz.getId() + ":" + attempt.getId();

            if (gamificationService.awardXp(user, baseScoreXp, SourceActivity.QUIZ_PASSED, passKey)) {
                xpAwarded += baseScoreXp;
            }

            if (percentage >= 100.0) {
                String perfectKey = "QUIZ_PERFECT:" + user.getId() + ":" + quiz.getId() + ":" + attempt.getId();
                if (gamificationService.awardXp(user, 30, SourceActivity.PERFECT_QUIZ_BONUS, perfectKey)) {
                    xpAwarded += 30;
                }
            }

            // Check course completion & certificate eligibility
            checkCourseCompletionEligibility(user, quiz);
        }

        AttemptResultDto result = convertAttemptToResult(attempt, true);
        result.setXpEarned(xpAwarded);
        return result;
    }

    @Transactional(readOnly = true)
    public AttemptResultDto getAttemptResult(UUID attemptId, User user) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz attempt not found: " + attemptId));

        if (!attempt.getUser().getId().equals(user.getId()) && user.getRole() != Role.ADMIN) {
            throw new ForbiddenActionException("Access denied: Cannot view attempt belonging to another user");
        }

        return convertAttemptToResult(attempt, true);
    }

    @Transactional
    public QuizDto createQuiz(CreateQuizDto createDto, User creator) {
        if (createDto.getScope() == null) {
            throw new InvalidInputException("Quiz scope is required (COURSE, MODULE, LESSON)");
        }

        Quiz quiz = new Quiz();
        quiz.setScope(createDto.getScope());
        quiz.setTitle(createDto.getTitle());
        quiz.setTitleTa(createDto.getTitleTa());
        quiz.setDescription(createDto.getDescription());
        quiz.setDescriptionTa(createDto.getDescriptionTa());
        quiz.setPassPercentage(createDto.getPassPercentage() != null ? createDto.getPassPercentage() : 80.0);
        quiz.setMaxAttempts(createDto.getMaxAttempts() != null ? createDto.getMaxAttempts() : 3);
        quiz.setTimeLimitMinutes(createDto.getTimeLimitMinutes());
        quiz.setMandatory(createDto.getMandatory() != null ? createDto.getMandatory() : true);
        quiz.setStatus(QuizStatus.DRAFT);
        quiz.setCreatedBy(creator);

        if (createDto.getScope() == QuizScope.COURSE && createDto.getCourseId() != null) {
            Course course = courseRepository.findById(createDto.getCourseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course not found: " + createDto.getCourseId()));
            quiz.setCourse(course);
        } else if (createDto.getScope() == QuizScope.MODULE && createDto.getModuleId() != null) {
            CourseModule module = courseModuleRepository.findById(createDto.getModuleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Module not found: " + createDto.getModuleId()));
            quiz.setModule(module);
        } else if (createDto.getScope() == QuizScope.LESSON && createDto.getLessonId() != null) {
            Lesson lesson = lessonRepository.findById(createDto.getLessonId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lesson not found: " + createDto.getLessonId()));
            quiz.setLesson(lesson);
        }

        Quiz savedQuiz = quizRepository.save(quiz);

        if (createDto.getQuestions() != null) {
            int qOrder = 1;
            for (QuizQuestionDto qDto : createDto.getQuestions()) {
                QuizQuestion question = new QuizQuestion();
                question.setQuiz(savedQuiz);
                question.setQuestionText(qDto.getQuestionText());
                question.setQuestionTextTa(qDto.getQuestionTextTa());
                question.setQuestionType(qDto.getQuestionType() != null ? qDto.getQuestionType() : QuestionType.MULTIPLE_CHOICE);
                question.setPoints(qDto.getPoints() != null ? qDto.getPoints() : 10);
                question.setDisplayOrder(qOrder++);

                if (qDto.getMediaId() != null) {
                    Media media = mediaRepository.findById(qDto.getMediaId()).orElse(null);
                    question.setMedia(media);
                }

                QuizQuestion savedQ = quizQuestionRepository.save(question);

                if (qDto.getOptions() != null) {
                    int oOrder = 1;
                    for (QuestionOptionDto oDto : qDto.getOptions()) {
                        QuestionOption opt = new QuestionOption();
                        opt.setQuestion(savedQ);
                        opt.setOptionText(oDto.getOptionText());
                        opt.setOptionTextTa(oDto.getOptionTextTa());
                        opt.setCorrect(Boolean.TRUE.equals(oDto.getIsCorrect()));
                        opt.setExplanationEn(oDto.getExplanationEn());
                        opt.setExplanationTa(oDto.getExplanationTa());
                        opt.setDisplayOrder(oOrder++);
                        questionOptionRepository.save(opt);
                    }
                }
            }
        }

        return convertToDto(savedQuiz);
    }

    @Transactional
    public QuizDto approveQuiz(UUID quizId, User approver) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found: " + quizId));

        if (quiz.getCreatedBy().getId().equals(approver.getId())) {
            throw new ForbiddenActionException("Prohibited Action: Contributor cannot approve their own submitted quiz.");
        }

        quiz.setStatus(QuizStatus.APPROVED);
        quiz.setApprovedBy(approver);
        quiz.setApprovedAt(Instant.now());
        Quiz saved = quizRepository.save(quiz);

        log.info("Quiz [{}] approved by {}", saved.getId(), approver.getId());
        return convertToDto(saved);
    }

    @Transactional
    public QuizDto publishQuiz(UUID quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found: " + quizId));

        if (quiz.getStatus() != QuizStatus.APPROVED && quiz.getStatus() != QuizStatus.DRAFT) {
            throw new InvalidInputException("Only APPROVED or DRAFT quizzes can be published.");
        }

        quiz.setStatus(QuizStatus.PUBLISHED);
        quiz.setPublishedAt(Instant.now());
        Quiz saved = quizRepository.save(quiz);

        log.info("Quiz [{}] published successfully", saved.getId());
        return convertToDto(saved);
    }

    private void checkCourseCompletionEligibility(User user, Quiz quiz) {
        Course course = null;
        if (quiz.getCourse() != null) {
            course = quiz.getCourse();
        } else if (quiz.getModule() != null) {
            course = quiz.getModule().getCourse();
        } else if (quiz.getLesson() != null) {
            course = quiz.getLesson().getModule().getCourse();
        }

        if (course == null) return;

        // Check if all mandatory published quizzes for this course are passed
        List<Quiz> courseQuizzes = quizRepository.findByCourseIdAndStatus(course.getId(), QuizStatus.PUBLISHED);
        boolean allPassed = true;
        double scoreSum = 0.0;
        int passedCount = 0;

        for (Quiz q : courseQuizzes) {
            if (Boolean.TRUE.equals(q.getMandatory())) {
                List<QuizAttempt> attempts = quizAttemptRepository.findByQuizIdAndUserIdOrderByAttemptNumberDesc(q.getId(), user.getId());
                Optional<QuizAttempt> passedOpt = attempts.stream().filter(a -> Boolean.TRUE.equals(a.getPassed())).findFirst();

                if (passedOpt.isPresent()) {
                    scoreSum += passedOpt.get().getPercentage();
                    passedCount++;
                } else {
                    allPassed = false;
                    break;
                }
            }
        }

        if (allPassed && passedCount > 0) {
            double avgScore = scoreSum / passedCount;
            if (avgScore >= 80.0) {
                certificateService.generateCertificate(user, course, avgScore);
                gamificationService.awardXp(user, 200, SourceActivity.COURSE_COMPLETED, "COURSE_COMPLETE:" + user.getId() + ":" + course.getId());
            }
        }
    }

    private QuizDto convertToDto(Quiz quiz) {
        QuizDto dto = new QuizDto();
        dto.setId(quiz.getId());
        dto.setScope(quiz.getScope());
        dto.setCourseId(quiz.getCourse() != null ? quiz.getCourse().getId() : null);
        dto.setModuleId(quiz.getModule() != null ? quiz.getModule().getId() : null);
        dto.setLessonId(quiz.getLesson() != null ? quiz.getLesson().getId() : null);
        dto.setTitle(quiz.getTitle());
        dto.setTitleTa(quiz.getTitleTa());
        dto.setDescription(quiz.getDescription());
        dto.setDescriptionTa(quiz.getDescriptionTa());
        dto.setPassPercentage(quiz.getPassPercentage());
        dto.setMaxAttempts(quiz.getMaxAttempts());
        dto.setTimeLimitMinutes(quiz.getTimeLimitMinutes());
        dto.setMandatory(quiz.getMandatory());
        dto.setStatus(quiz.getStatus());
        return dto;
    }

    private AttemptResultDto convertAttemptToResult(QuizAttempt attempt, boolean includeExplanations) {
        AttemptResultDto result = new AttemptResultDto();
        result.setAttemptId(attempt.getId());
        result.setQuizId(attempt.getQuiz().getId());
        result.setAttemptNumber(attempt.getAttemptNumber());
        result.setScoreEarned(attempt.getScoreEarned());
        result.setTotalPossibleScore(attempt.getTotalPossibleScore());
        result.setPercentage(attempt.getPercentage());
        result.setPassed(attempt.getPassed());
        result.setStatus(attempt.getStatus());
        result.setStartedAt(attempt.getStartedAt());
        result.setSubmittedAt(attempt.getSubmittedAt());

        if (includeExplanations && attempt.getStatus() != AttemptStatus.STARTED) {
            List<QuizQuestion> questions = quizQuestionRepository.findByQuizIdOrderByDisplayOrderAsc(attempt.getQuiz().getId());
            List<QuizAttemptAnswer> userAnswers = quizAttemptAnswerRepository.findByAttemptId(attempt.getId());

            Map<UUID, QuizAttemptAnswer> answerMap = userAnswers.stream()
                    .collect(Collectors.toMap(a -> a.getQuestion().getId(), a -> a, (k1, k2) -> k1));

            List<QuizQuestionDto> questionDtos = new ArrayList<>();
            for (QuizQuestion q : questions) {
                QuizQuestionDto qDto = new QuizQuestionDto();
                qDto.setId(q.getId());
                qDto.setQuestionText(q.getQuestionText());
                qDto.setQuestionTextTa(q.getQuestionTextTa());
                qDto.setQuestionType(q.getQuestionType());
                qDto.setPoints(q.getPoints());
                qDto.setDisplayOrder(q.getDisplayOrder());

                List<QuestionOption> options = questionOptionRepository.findByQuestionIdOrderByDisplayOrderAsc(q.getId());
                QuizAttemptAnswer ua = answerMap.get(q.getId());

                List<QuestionOptionDto> oDtos = options.stream().map(o -> {
                    QuestionOptionDto oDto = new QuestionOptionDto();
                    oDto.setId(o.getId());
                    oDto.setOptionText(o.getOptionText());
                    oDto.setOptionTextTa(o.getOptionTextTa());
                    oDto.setDisplayOrder(o.getDisplayOrder());
                    oDto.setIsCorrect(o.getCorrect());
                    oDto.setExplanationEn(o.getExplanationEn());
                    oDto.setExplanationTa(o.getExplanationTa());
                    return oDto;
                }).collect(Collectors.toList());

                qDto.setOptions(oDtos);
                questionDtos.add(qDto);
            }
            result.setQuestionsWithExplanations(questionDtos);
        }

        return result;
    }
}
