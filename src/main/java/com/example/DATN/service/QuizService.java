package com.example.DATN.service;

import com.example.DATN.dto.quiz.*;
import com.example.DATN.entity.*;
import com.example.DATN.entity.document.*;
import com.example.DATN.entity.enums.AttemptStatus;
import com.example.DATN.entity.enums.QuizStatus;
import com.example.DATN.exception.AppException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.repository.*;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class QuizService {

    QuizRepository quizRepository;
    QuizAttemptRepository quizAttemptRepository;
    FeedbackRepository feedbackRepository;
    QuizContentRepository quizContentRepository;
    AttemptDetailRepository attemptDetailRepository;
    AiService aiService;
    GroupRepository groupRepository;
    UserRepository userRepository;
    GroupMembershipRepository groupMembershipRepository;

    //  CREATE
    public QuizResponse createQuiz(QuizCreateRequest request, Long teacherId) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        if (!teacher.isTeacher()) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }
        if (request.getStartAt() != null && request.getEndAt() != null
                && request.getEndAt().isBefore(request.getStartAt())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        if (request.getQuestions() == null || request.getQuestions().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        boolean isGroup = request.getGroupId() != null;
        QuizStatus status = isGroup ? QuizStatus.GROUP : QuizStatus.PENDING;

        Group group = isGroup
                ? groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new AppException(ErrorCode.NOT_IN_GROUP))
                : null;

        Quiz quiz = Quiz.builder()
                .creator(teacher)
                .group(group)
                .title(request.getTitle())
                .description(request.getDescription())
                .status(status)
                .startAt(request.getStartAt())
                .endAt(request.getEndAt())
                .time(request.getTime())
                .maxAttempt(request.getMaxAttempt())
                .allowAiReview(request.isAllowAiReview())
                .build();

        quizRepository.save(quiz);

        ContentQuiz contentQuiz = buildContentQuiz(quiz.getId(), request);
        quizContentRepository.save(contentQuiz);

        quiz.setContentQuizId(contentQuiz.getId());
        quizRepository.save(quiz);

        return toQuizResponse(quiz);
    }

    //  UPDATE
    public QuizResponse updateQuiz(Long quizId, QuizCreateRequest request, Long teacherId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_QUIZ));
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!quiz.getCreator().getId().equals(teacher.getId())) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }
        if (quiz.getStatus() == QuizStatus.ACTIVE) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setTime(request.getTime());
        quiz.setMaxAttempt(request.getMaxAttempt());
        quiz.setStartAt(request.getStartAt());
        quiz.setEndAt(request.getEndAt());
        quiz.setAllowAiReview(request.isAllowAiReview());
        quizRepository.save(quiz);

        ContentQuiz contentQuiz = quizContentRepository
                .findById(quiz.getContentQuizId())
                .orElseThrow(() -> new AppException(ErrorCode.QUIZ_CONTENT_NOT_FOUND));

        Map<String, Question> questionOld = contentQuiz.getQuestions().stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        List<Question> questions = request.getQuestions().stream()
                .map(req -> {
                    if (req.getId() != null && questionOld.containsKey(req.getId())) {
                        Question old = questionOld.get(req.getId());
                        old.setQuestionText(req.getQuestionText());
                        old.setType(req.getType());
                        old.setPoint(req.getPoint());
                        old.setOrder(req.getOrder());
                        old.setExplanation(req.getExplanation());
                        old.setOptions(buildOptions(req.getOptions()));
                        return old;
                    }
                    return Question.builder()
                            .id(UUID.randomUUID().toString())
                            .questionText(req.getQuestionText())
                            .type(req.getType())
                            .point(req.getPoint())
                            .order(req.getOrder())
                            .explanation(req.getExplanation())
                            .options(buildOptions(req.getOptions()))
                            .build();
                }).toList();

        contentQuiz.setQuestions(questions);
        quizContentRepository.save(contentQuiz);

        return toQuizResponse(quiz);
    }

    //  ACTIVATE (quiz nhóm, do teacher)
    public QuizResponse activateGroupQuiz(Long quizId, Long teacherId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_QUIZ));
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!quiz.getCreator().getId().equals(teacher.getId())) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }
        if (quiz.getStatus() != QuizStatus.GROUP) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        quiz.setStatus(QuizStatus.ACTIVE);
        quizRepository.save(quiz);
        return toQuizResponse(quiz);
    }

    //  ADMIN REVIEW
    public QuizResponse reviewQuiz(Long quizId, boolean approved, String note) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_QUIZ));

        if (quiz.getGroup() != null) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }
        if (quiz.getStatus() != QuizStatus.PENDING) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        quiz.setStatus(approved ? QuizStatus.ACTIVE : QuizStatus.REJECTED);
        quiz.setNote(note);
        quizRepository.save(quiz);

        return toQuizResponse(quiz);
    }

    public Page<QuizResponse> getPendingQuizzes(Pageable pageable) {
        return quizRepository
                .findByGroupIsNullAndStatus(QuizStatus.PENDING, pageable)
                .map(this::toQuizResponse);
    }

    //  STUDENT: START
    public Map<String, Object> startQuiz(Long quizId, Long studentId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_QUIZ));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        validateQuizAvailable(quiz);
        validateCanAttempt(quiz, student);

        int numAttempt = quizAttemptRepository
                .countByQuizIdAndUserId(quizId, student.getId()) + 1;

        QuizAttempt quizAttempt = QuizAttempt.builder()
                .quiz(quiz)
                .user(student)
                .attemptNumber(numAttempt)
                .status(AttemptStatus.IN_PROGRESS)
                .startAt(LocalDateTime.now())
                .build();

        quizAttempt = quizAttemptRepository.save(quizAttempt);

        AttemptDetail attemptDetail = AttemptDetail.builder()
                .attemptId(quizAttempt.getId())
                .quizId(quizId)
                .userId(student.getId())
                .createAt(Instant.now())
                .build();

        attemptDetail = attemptDetailRepository.save(attemptDetail);

        quizAttempt.setDetailQuizId(attemptDetail.getId());
        quizAttemptRepository.save(quizAttempt);

        ContentQuiz contentQuiz = quizContentRepository.findByQuizId(quizId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_QUIZ));

        QuizDetailResponse quizDetail = toQuizDetailResponse(quiz, contentQuiz);

        return Map.of(
                "attemptId", quizAttempt.getId(),
                "quiz", quizDetail,
                // deadline tính từ thời điểm học sinh bắt đầu (đúng)
                "serverDeadline", quizAttempt.getStartAt().plusMinutes(quiz.getTime())
        );
    }

    //  STUDENT: SUBMIT
    public AttemptResponse submitAttempt(
            Long attemptId,
            List<AnswerRequest> answers,
            Long studentId,
            boolean autoSubmit
    ) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_QUIZ));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!attempt.getUser().getId().equals(student.getId())) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        LocalDateTime deadline = attempt.getStartAt()
                .plusMinutes(attempt.getQuiz().getTime());

        if (!autoSubmit && LocalDateTime.now().isAfter(deadline)) {
            throw new AppException(ErrorCode.QUIZ_EXPIRED);
        }

        ContentQuiz contentQuiz = quizContentRepository
                .findByQuizId(attempt.getQuiz().getId())
                .orElseThrow(() -> new AppException(ErrorCode.QUIZ_CONTENT_NOT_FOUND));

        List<Question> questions = contentQuiz.getQuestions();
        List<Answer> gradedAnswers = gradeAnswers(answers, questions);

        double earned = gradedAnswers.stream()
                .mapToDouble(a -> a.getPointsEarned() != null ? a.getPointsEarned() : 0)
                .sum();
        double total = questions.stream()
                .mapToDouble(q -> q.getPoint() != null ? q.getPoint() : 1.0)
                .sum();
        double percent = total > 0 ? earned / total * 100 : 0;

        attempt.setStatus(autoSubmit ? AttemptStatus.AUTO_SUBMITTED : AttemptStatus.SUBMITTED);
        attempt.setScore(earned);
        attempt.setTotalPoints(total);
        attempt.setScorePrecent(Math.round(percent * 10.0) / 10.0);
        attempt.setSubmitAt(LocalDateTime.now());
        quizAttemptRepository.save(attempt);

        AttemptDetail detail = attemptDetailRepository
                .findByAttemptId(attemptId)
                .orElse(AttemptDetail.builder()
                        .attemptId(attemptId)
                        .quizId(attempt.getQuiz().getId())
                        .userId(student.getId())
                        .createAt(Instant.now())
                        .build());
        detail.setAnswers(gradedAnswers);
        attemptDetailRepository.save(detail);

        double bestScore = quizAttemptRepository
                .findBestScore(attempt.getQuiz().getId(), student.getId())
                .orElse(earned);
        boolean canRetake = canRetake(attempt.getQuiz(), student, attempt);

        return buildAttemptResponse(attempt, gradedAnswers, questions, bestScore, canRetake);
    }

    //  STUDENT: AI REVIEW
    public AiReviewResponse aiReviewRequest(Long attemptId, Long studentId) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_QUIZ));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!attempt.getUser().getId().equals(student.getId())) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }
        if (attempt.getStatus() == AttemptStatus.IN_PROGRESS) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }
        if (!attempt.getQuiz().isAllowAiReview()) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        ContentQuiz contentQuiz = quizContentRepository
                .findByQuizId(attempt.getQuiz().getId())
                .orElseThrow(() -> new AppException(ErrorCode.NO_PERMISSION));

        // Đã từng gọi AI review → trả cache
        if (attempt.isAiReviewRequest()) {
            AttemptDetail detail = attemptDetailRepository.findByAttemptId(attemptId)
                    .orElseThrow(() -> new AppException(ErrorCode.NO_PERMISSION));
            return toAiReviewResponse(detail.getReview(), contentQuiz.getQuestions());
        }

        AttemptDetail detail = attemptDetailRepository.findByAttemptId(attemptId)
                .orElseThrow(() -> new AppException(ErrorCode.NO_PERMISSION));

        AiReview aiReview = aiService.generateAiReview(contentQuiz, detail);
        detail.setReview(aiReview);
        attemptDetailRepository.save(detail);

        // Khoá retake sau khi dùng AI review
        attempt.setAiReviewRequest(true);
        quizAttemptRepository.save(attempt);

        return toAiReviewResponse(aiReview, contentQuiz.getQuestions());
    }

    //  STUDENT: MY ATTEMPTS
    public List<AttemptResponse> getMyAttempts(Long quizId, Long studentId) {
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        List<QuizAttempt> attempts = quizAttemptRepository
                .findByQuizIdAndUserIdOrderByAttemptNumberAsc(quizId, student.getId());

        double bestScore = quizAttemptRepository
                .findBestScore(quizId, student.getId())
                .orElse(0.0);

        ContentQuiz content = quizContentRepository.findByQuizId(quizId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_QUIZ));

        return attempts.stream()
                .map(a -> {
                    AttemptDetail detail = a.getDetailQuizId() != null
                            ? attemptDetailRepository.findById(a.getDetailQuizId()).orElse(null)
                            : null;
                    List<Answer> answers =
                            (detail != null && detail.getAnswers() != null)
                                    ? detail.getAnswers()
                                    : List.of();
                    boolean canRetake = canRetake(a.getQuiz(), student, a);
                    return buildAttemptResponse(
                            a, answers, content.getQuestions(), bestScore, canRetake);
                })
                .collect(Collectors.toList());
    }

    //  TEACHER: SUBMISSIONS
    public Page<AttemptResponse> getSubmissions(
            Long quizId, Long teacherId, Pageable pageable) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_QUIZ));
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!quiz.getCreator().getId().equals(teacher.getId())) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        return quizAttemptRepository
                .findByQuizIdAndStatusIn(
                        quizId,
                        List.of(AttemptStatus.SUBMITTED, AttemptStatus.AUTO_SUBMITTED),
                        pageable)
                .map(a -> toAttemptResponse(a, false));
    }

    public AttemptResponse getSubmissionDetail(Long attemptId, Long teacherId) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_QUIZ));
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!attempt.getQuiz().getCreator().getId().equals(teacher.getId())) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }
        return toAttemptResponse(attempt, true);
    }

    //  TEACHER: FEEDBACK
    public FeedbackResponse addFeedback(
            Long attemptId, String questionId, String content, Long teacherId) {
        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_QUIZ));
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (!attempt.getQuiz().getCreator().getId().equals(teacher.getId())) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        FeedbackTeacher feedback = FeedbackTeacher.builder()
                .attempt(attempt)
                .teacher(teacher)
                .questionId(questionId)
                .content(content)
                // createAt được set tự động bởi @PrePersist trong entity
                .build();
        feedback = feedbackRepository.save(feedback);
        return toFeedbackResponse(feedback);
    }

    //  PUBLIC / GROUP QUIZ LIST
    public Slice<QuizResponse> getPublicQuizzes(int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, "createAt"));
        return quizRepository
                .findByStatusAndGroupIsNull(QuizStatus.ACTIVE, pageable)
                .map(this::toQuizResponse);
    }
    public List<QuizResponse> getQuizzesByGroup(Long groupId, Long userId) {
        groupRepository.findById(groupId)
                .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_FOUND));

        if (!groupMembershipRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        return quizRepository
                .findByGroupIdAndStatusOrderByCreateAtDesc(groupId, QuizStatus.ACTIVE)
                .stream()
                .map(this::toQuizResponse)
                .toList();
    }

    public List<QuizResponse> getMyQuizzes(Long teacherId) {
        return quizRepository
                .findByCreator_IdOrderByCreateAtDesc(teacherId)
                .stream()
                .map(this::toQuizResponse)
                .toList();
    }

    //  AUTO SUBMIT (scheduler)
    @Scheduled(fixedDelay = 30_000)
    public void autoSubmitExpiredAttempts() {
        List<QuizAttempt> expired = quizAttemptRepository
                .findExpiredAttempts(LocalDateTime.now().minusSeconds(1));

        for (QuizAttempt attempt : expired) {

            LocalDateTime deadline = attempt.getStartAt()
                    .plusMinutes(attempt.getQuiz().getTime());

            if (LocalDateTime.now().isAfter(deadline)) {
                try {
                    AttemptDetail detail = attemptDetailRepository
                            .findByAttemptId(attempt.getId()).orElse(null);
                    List<AnswerRequest> saved = detail != null
                            ? detail.getAnswers().stream()
                            .map(a -> new AnswerRequest(
                                    a.getQuestionId(),
                                    a.getSelectAnswer(),
                                    a.getTextAnswer()))
                            .toList()
                            : List.of();

                    submitAttempt(attempt.getId(), saved,
                            attempt.getUser().getId(), true);
                } catch (Exception e) {
                    log.error("Auto-submit failed for attemptId={}", attempt.getId(), e);
                }
            }
        }
    }

    //  PRIVATE HELPERS
    private void validateQuizAvailable(Quiz quiz) {
        if (quiz.getStatus() != QuizStatus.ACTIVE) {
            throw new AppException(ErrorCode.QUIZ_NOT_ACTIVE);
        }
        LocalDateTime now = LocalDateTime.now();
        if (quiz.getStartAt() != null && now.isBefore(quiz.getStartAt())) {
            throw new AppException(ErrorCode.QUIZ_NOT_STARTED);
        }
        if (quiz.getEndAt() != null && now.isAfter(quiz.getEndAt())) {
            throw new AppException(ErrorCode.QUIZ_EXPIRED);
        }
    }

    private void validateCanAttempt(Quiz quiz, User student) {
        quizAttemptRepository
                .findByQuizIdAndUserIdAndStatus(
                        quiz.getId(), student.getId(), AttemptStatus.IN_PROGRESS)
                .ifPresent(a -> {
                    throw new AppException(ErrorCode.ATTEMPT_IN_PROGRESS);
                });

        quizAttemptRepository
                .findTopByQuizIdAndUserIdOrderByAttemptNumberDesc(
                        quiz.getId(), student.getId())
                .ifPresent(last -> {
                    if (last.isAiReviewRequest()) {
                        throw new AppException(ErrorCode.AI_REVIEW_LOCKED);
                    }
                    if (last.getAttemptNumber() >= quiz.getMaxAttempt()) {
                        throw new AppException(ErrorCode.MAX_ATTEMPT_REACHED);
                    }
                });
    }

    private boolean canRetake(Quiz quiz, User student, QuizAttempt current) {
        if (current.isAiReviewRequest()) return false;
        if (current.getAttemptNumber() >= quiz.getMaxAttempt()) return false;
        if (quiz.getEndAt() != null && LocalDateTime.now().isAfter(quiz.getEndAt())) return false;
        return true;
    }

    //  BUILD HELPERS
    private ContentQuiz buildContentQuiz(Long quizId, QuizCreateRequest request) {
        return ContentQuiz.builder()
                .quizId(quizId)
                .questions(buildQuestions(request))
                .build();
    }

    private List<Question> buildQuestions(QuizCreateRequest request) {
        return request.getQuestions().stream()
                .map(q -> Question.builder()
                        .id(UUID.randomUUID().toString())
                        .questionText(q.getQuestionText())
                        .type(q.getType())
                        .point(q.getPoint())
                        .order(q.getOrder())
                        .explanation(q.getExplanation())
                        .options(buildOptions(q.getOptions()))
                        .build())
                .toList();
    }

    private List<QuestionOption> buildOptions(List<OptionRequest> options) {
        if (options == null) return null;
        return options.stream()
                .map(o -> QuestionOption.builder()
                        .text(o.getText())
                        .isCorrect(o.isCorrect())
                        .build())
                .toList();
    }

    private List<Answer> gradeAnswers(List<AnswerRequest> submitted, List<Question> questions) {
        Map<String, AnswerRequest> answerMap = new HashMap<>();
        if (submitted != null) {
            submitted.forEach(a -> answerMap.put(a.getQuestionId(), a));
        }
        Instant now = Instant.now();
        return questions.stream()
                .map(q -> {
                    AnswerRequest sub = answerMap.get(q.getId());
                    boolean correct = false;
                    double earned = 0.0;
                    if (sub != null) {
                        correct = switch (q.getType()) {
                            case SINGLE_CHOICE, TRUE_FALSE -> isSingleCorrect(sub, q);
                            case MULTIPLE_CHOICE -> isMultipleCorrect(sub, q);
                            case SHORT_TEXT -> false;
                        };
                        earned = correct ? (q.getPoint() != null ? q.getPoint() : 1.0) : 0.0;
                    }
                    return Answer.builder()
                            .questionId(q.getId())
                            .selectAnswer(sub != null ? sub.getSelectedOptionIndexes() : null)
                            .textAnswer(sub != null ? sub.getTextAnswer() : null)
                            .isCorrect(correct)
                            .pointsEarned(earned)
                            .answeredAt(now)
                            .build();
                })
                .toList();
    }

    private boolean isSingleCorrect(AnswerRequest ans, Question q) {
        if (ans.getSelectedOptionIndexes() == null
                || ans.getSelectedOptionIndexes().isEmpty()) {
            return false;
        }
        int chosen = ans.getSelectedOptionIndexes().get(0);
        if (chosen < 0 || chosen >= q.getOptions().size()) return false;
        return q.getOptions().get(chosen).isCorrect();
    }

    private boolean isMultipleCorrect(AnswerRequest ans, Question q) {
        if (ans.getSelectedOptionIndexes() == null) return false;
        Set<Integer> chosen = new HashSet<>(ans.getSelectedOptionIndexes());
        Set<Integer> correct = IntStream.range(0, q.getOptions().size())
                .filter(i -> q.getOptions().get(i).isCorrect())
                .boxed()
                .collect(Collectors.toSet());
        return chosen.equals(correct);
    }

    //  MAPPERS

    private QuizResponse toQuizResponse(Quiz quiz) {
        Profile p = quiz.getCreator().getProfile();
        return QuizResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .status(quiz.getStatus())
                .time(quiz.getTime())
                .maxAttempts(quiz.getMaxAttempt())
                .startAt(quiz.getStartAt())
                .endAt(quiz.getEndAt())
                .createdAt(quiz.getCreateAt())
                .note(quiz.getNote())
                .creatorId(quiz.getCreator().getId())
                .creatorName(p != null ? p.getFullName() : "")
                .build();
    }

    private QuizDetailResponse toQuizDetailResponse(Quiz quiz, ContentQuiz contentQuiz) {
        List<QuestionResponse> questions = contentQuiz.getQuestions().stream()
                .sorted(Comparator.comparingInt(Question::getOrder))
                .map(q -> {
                    List<OptionResponse> options = q.getOptions() != null
                            ? q.getOptions().stream()
                            .map(o -> OptionResponse.builder()
                                    .text(o.getText())
                                    .build())
                            .toList()
                            : List.of();
                    return QuestionResponse.builder()
                            .id(q.getId())
                            .questionText(q.getQuestionText())
                            .type(q.getType())
                            .points(q.getPoint())
                            .explanation(q.getExplanation())
                            .options(options)
                            .build();
                }).toList();

        return QuizDetailResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .time(quiz.getTime())
                .maxAttempts(quiz.getMaxAttempt())
                .allowAiReview(quiz.isAllowAiReview())
                .startAt(quiz.getStartAt())
                .endAt(quiz.getEndAt())
                .questions(questions)
                .build();
    }

    private AttemptResponse toAttemptResponse(QuizAttempt attempt, boolean isDetail) {
        List<AnswerResponse> answers = List.of();
        List<FeedbackResponse> feedbacks = List.of();

        if (isDetail && attempt.getDetailQuizId() != null) {
            AttemptDetail detail = attemptDetailRepository
                    .findById(attempt.getDetailQuizId())
                    .orElseThrow(() -> new AppException(ErrorCode.NO_PERMISSION));
            ContentQuiz content = quizContentRepository
                    .findByQuizId(detail.getQuizId())
                    .orElseThrow(() -> new AppException(ErrorCode.NO_PERMISSION));
            List<Answer> gradedAnswers =
                    detail.getAnswers() != null ? detail.getAnswers() : List.of();

            answers = buildAttemptResponse(
                    attempt, gradedAnswers, content.getQuestions(), 0, false
            ).getAnswers();

            feedbacks = feedbackRepository
                    .findByAttemptIdOrderByCreateAtAsc(attempt.getId())
                    .stream()
                    .map(this::toFeedbackResponse)
                    .collect(Collectors.toList());
        }

        User student = attempt.getUser();
        Profile profile = student.getProfile();

        return AttemptResponse.builder()
                .attemptId(attempt.getId())
                .studentId(student.getId())
                .studentName(profile != null ? profile.getFullName() : "")
                .studentAvatar(profile != null ? profile.getAvatarUrl() : "")
                .attemptNumber(attempt.getAttemptNumber())
                .status(attempt.getStatus())
                .score(attempt.getScore())
                .scorePercent(attempt.getScorePrecent())
                .totalPoints(attempt.getTotalPoints())
                .submittedAt(attempt.getSubmitAt())
                .aiReview(attempt.isAiReviewRequest())
                .answers(answers)
                .feedbacks(feedbacks)
                .canRetake(false)
                .build();
    }

    private AttemptResponse buildAttemptResponse(
            QuizAttempt attempt,
            List<Answer> gradedAnswers,
            List<Question> questions,
            double bestScore,
            boolean canRetake
    ) {
        Map<String, Question> questionMap = questions.stream()
                .collect(Collectors.toMap(Question::getId, q -> q));

        List<AnswerResponse> answerResult =
                Optional.ofNullable(gradedAnswers)
                        .orElse(Collections.emptyList())
                        .stream()
                        .map(a -> {
                            Question q = questionMap.get(a.getQuestionId());
                            return AnswerResponse.builder()
                                    .questionId(a.getQuestionId())
                                    .questionText(q != null ? q.getQuestionText() : null)
                                    .selectedOptionIndexes(a.getSelectAnswer())
                                    .textAnswer(a.getTextAnswer())
                                    .isCorrect(a.isCorrect())
                                    .pointsEarned(a.getPointsEarned())
                                    .maxPoints(q != null ? q.getPoint() : 0)
                                    .explanation(q != null ? q.getExplanation() : null)
                                    .build();
                        })
                        .toList();

        return AttemptResponse.builder()
                .attemptId(attempt.getId())
                .attemptNumber(attempt.getAttemptNumber())
                .status(attempt.getStatus())
                .score(attempt.getScore())
                .scorePercent(attempt.getScorePrecent())
                .totalPoints(attempt.getTotalPoints())
                .submittedAt(attempt.getSubmitAt())
                .canRetake(canRetake)
                .aiReview(attempt.isAiReviewRequest())
                .bestScore(bestScore)
                .answers(answerResult)
                .build();
    }

    private AiReviewResponse toAiReviewResponse(AiReview review, List<Question> questions) {
        if (review == null) return null;

        List<QuestionAnalysisResponse> perQ = new ArrayList<>();
        if (review.getPerQuestion() != null) {
            for (QuestionAnalysis qa : review.getPerQuestion()) {
                String qText = qa.getQuestionIndex() < questions.size()
                        ? questions.get(qa.getQuestionIndex()).getQuestionText() : "";
                perQ.add(QuestionAnalysisResponse.builder()
                        .questionIndex(qa.getQuestionIndex())
                        .questionText(qText)
                        .analysis(qa.getAnalysis())
                        .correctApproach(qa.getCorrectApproach())
                        .build());
            }
        }

        return AiReviewResponse.builder()
                .overallAnalysis(review.getOverallAnalysis())
                .weaknessAreas(review.getWeaknessAreas())
                .studyRoadmap(review.getStudyRoadmap())
                .perQuestion(perQ)
                .generatedAt(review.getGeneratedAt())
                .build();
    }

    private FeedbackResponse toFeedbackResponse(FeedbackTeacher f) {
        Profile p = f.getTeacher().getProfile();
        return FeedbackResponse.builder()
                .id(f.getId())
                .teacherId(f.getTeacher().getId())
                .teacherName(p != null ? p.getFullName() : "")
                .teacherAvatar(p != null ? p.getAvatarUrl() : "")
                .questionId(f.getQuestionId())
                .content(f.getContent())
                .createdAt(f.getCreateAt())
                .build();
    }
}