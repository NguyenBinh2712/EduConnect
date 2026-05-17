package com.example.DATN.service;

import com.example.DATN.dto.chat.CreateChatGroup;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Transactional
public class QuizService {
    QuizRepository quizRepository;
    QuizAttemptRepository quizAttemptRepository;
    FeedbackRepository feedbackRepository;
    QuizContentRepository quizContentRepository;
    AttemptDetailRepository attemptDetailRepository;
    AiService aiService;
    GroupRepository groupRepository;


    public QuizResponse createQuiz(QuizCreateRequest request, User teacher){
        if(!teacher.isTeacher()){
            throw new AppException(ErrorCode.NO_PERMISSION);
        }
        boolean isGroup=request.getGroupId()!=null;
        var status= isGroup ? QuizStatus.CLOSE : QuizStatus.PENDING;
        if(request.getStartAt()!=null&&request.getEndAt()!=null &&
                request.getEndAt().isBefore(request.getStartAt())){
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        Group group= isGroup ? groupRepository.findById(request.getGroupId())
                .orElseThrow(()->new AppException(ErrorCode.NOT_IN_GROUP))
                : null;

        Quiz quiz= Quiz.builder()
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
        ContentQuiz contentQuiz=buildContentQuiz(quiz.getId(), request);
        quizContentRepository.save(contentQuiz);
        quiz.setContentQuizId(contentQuiz.getId());
        quizRepository.save(quiz);
        return toQuizResponse(quiz);
    }

    private ContentQuiz buildContentQuiz(
            Long quizId,
            QuizCreateRequest request
    ){

        List<Question> questions = buildQuestions(request);

        return ContentQuiz.builder()
                .quizId(quizId)
                .questions(questions)
                .build();
    }

    private List<Question> buildQuestions(
            QuizCreateRequest request
    ){

        return request.getQuestions().stream()
                .map(q -> Question.builder()
                        .id(UUID.randomUUID().toString())
                        .questionText(q.getQuestionText())
                        .type(q.getType())
                        .point(q.getPoint())
                        .order(q.getOrder())
                        .explanation(q.getExplanation())
                        .options(buildOptions(q.getOptions()))
                        .build()
                )
                .toList();
    }

    private List<QuestionOption> buildOptions(
            List<QuestionOption> options
    ){

        if(options == null){
            return null;
        }

        return options.stream()
                .map(o -> QuestionOption.builder()
                        .text(o.getText())
                        .isCorrect(o.isCorrect())
                        .build()
                )
                .toList();
    }
    private QuizResponse toQuizResponse(Quiz quiz) {
        Profile p = quiz.getCreator().getProfile();
        return QuizResponse.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .status(quiz.getStatus())
                .time(quiz.getTime())
                .maxAttempts(quiz.getMaxAttempt())
                .startAt(quiz.getStartAt())
                .endAt(quiz.getEndAt())
                .createdAt(quiz.getCreateAt())
                .creatorId(quiz.getCreator().getId())
                .creatorName(p != null ? p.getFullName() : "")
                .build();
    }

    public QuizResponse updateQuiz(Long quizId, QuizCreateRequest request,User teacher) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND_QUIZ));
        if (!quiz.getCreator().getId().equals(teacher.getId())) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }
        if (quiz.getStatus() == QuizStatus.ACTIVE ) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }
        quiz.setTitle(request.getTitle());
        quiz.setTime(request.getTime());
        quiz.setMaxAttempt(request.getMaxAttempt());
        quiz.setStartAt(request.getStartAt());
        quiz.setEndAt(request.getEndAt());
        quizRepository.save(quiz);

        ContentQuiz contentQuiz = quizContentRepository
                .findById(quiz.getContentQuizId()).orElseThrow(() -> new AppException(ErrorCode.QUIZ_CONTENT_NOT_FOUND));

        Map<String, Question> questionOld = contentQuiz.getQuestions().stream()
                .collect(Collectors.toMap(
                        Question::getId,
                        q -> q
                ));
        List<Question> questions = request.getQuestions().stream()
                .map(
                        question -> {
                            if (question.getId() != null && questionOld.containsKey(question.getId())) {
                                Question quesOld = questionOld.get(question.getId());
                                quesOld.setQuestionText(question.getQuestionText());
                                quesOld.setType(question.getType());
                                quesOld.setPoint(question.getPoint());
                                quesOld.setOrder(question.getOrder());
                                quesOld.setExplanation(question.getExplanation());
                                quesOld.setOptions(
                                        buildOptions(question.getOptions())
                                );
                                return quesOld;
                            }
                            return Question.builder()
                                    .id(UUID.randomUUID().toString())
                                    .questionText(question.getQuestionText())
                                    .type(question.getType())
                                    .point(question.getPoint())
                                    .order(question.getOrder())
                                    .explanation(question.getExplanation())
                                    .options(buildOptions(question.getOptions()))
                                    .build();

                        }).toList();
    contentQuiz.setQuestions(questions);
    quizContentRepository.save(contentQuiz);

    return toQuizResponse(quiz);
    }

    // ds hs nọp bai va diem
    public Page<AttemptResponse> getSubmissions(Long quizId, User teacher, Pageable pageable){
        Quiz quiz=quizRepository.findById(quizId)
                .orElseThrow(()->new AppException(ErrorCode.NOT_FOUND_QUIZ));
        if(!quiz.getCreator().getId().equals(teacher.getId())){
            throw new AppException(ErrorCode.NO_PERMISSION);
        }
        Page<QuizAttempt> attempts=quizAttemptRepository.findByQuizIdAndStatusIn(quizId,
                List.of(AttemptStatus.SUBMITTED, AttemptStatus.AUTO_SUBMITTED),
                pageable);

        return attempts.map(a -> toAttemptResponse(a, false));
    }

    // gv xem chi tiet bai lam hs
    public AttemptResponse getSubmissionDetail(Long attemptId,User teacher){
        QuizAttempt attempt=quizAttemptRepository.findById(attemptId)
                .orElseThrow(()->new AppException(ErrorCode.NO_PERMISSION));
        if(!attempt.getQuiz().getCreator().getId().equals(teacher.getId())){
            throw new AppException(ErrorCode.NO_PERMISSION);
        }
        return toAttemptResponse(attempt,true);
    }

    private AttemptResponse toAttemptResponse(QuizAttempt attempt, boolean isDetail){
        List<AnswerResponse> answers=List.of();
        List<FeedbackResponse> feedbacks=List.of();


        if(isDetail && attempt.getDetailQuizId()!=null) {
            AttemptDetail detail = attemptDetailRepository.findById(attempt.getDetailQuizId())
                    .orElseThrow(() -> new AppException(ErrorCode.NO_PERMISSION));
            ContentQuiz content = quizContentRepository.findByQuizId(detail.getQuizId())
                    .orElseThrow(() -> new AppException(ErrorCode.NO_PERMISSION));
            if (detail != null) {
                answers = buildAttemptResponse(attempt, detail.getAnswers(), content.getQuestions(), 0, false)
                        .getAnswers();
            }
             feedbacks = feedbackRepository.findByAttemptIdOrderByCreatedAtAsc(attempt.getId())
                    .stream().map(this::toFeedbackResponse).collect(Collectors.toList());
        }

            User student= attempt.getUser();
            Profile profile=student.getProfile();

            return AttemptResponse.builder()
                    .attemptId(attempt.getId())
                    .studentId(student.getId())
                    .studentName(profile != null ? profile.getFullName() : "")
                    .studentAvatar(profile != null ? profile.getAvatarUrl() : "")
                    .attemptNumber(attempt.getAttemptNumber())
                    .status(attempt.getStatus())
                    .score(attempt.getScore())
                    .scorePercent(attempt.getScorePrecent())
                    .submittedAt(attempt.getSubmitAt())
                    .aiReview(attempt.isAiReviewRequest())
                    .answers(answers)
                    .feedbacks(feedbacks)
                    .canRetake(false)
                    .build();

    }

    //build response attempt quiz
    private AttemptResponse buildAttemptResponse(
            QuizAttempt attempt,
            List<Answer> gradedAnswers,
            List<Question> questions,
            double bestScore,
            boolean canRetake
    ) {
       Map<String,Question> questionMap=questions.stream()
               .collect(Collectors.toMap(
                       q->q.getId(),
                       q->q
               ));
       List<AnswerResponse> answerResult=gradedAnswers.stream()
               .map(a->{
                   Question question=questionMap.get(a.getQuestionId());
                   return AnswerResponse.builder()
                           .questionId(a.getQuestionId())
                           .questionText(question!=null? question.getQuestionText():null)
                           .selectedOptionIndexes(a.getSelectAnswer())
                           .textAnswer(a.getTextAnswer())
                           .isCorrect(a.isCorrect())
                           .pointsEarned(a.getPointsEarned())
                           .maxPoints(question!=null?question.getPoint():0)
                           .explanation(question!=null?question.getExplanation():null)
                           .build();
               }).toList();

       boolean isAiReview= attempt.isAiReviewRequest();
       return AttemptResponse.builder()
               .attemptId(attempt.getId())
               .attemptNumber(attempt.getAttemptNumber())
               .status(attempt.getStatus())
               .score(attempt.getScore())
               .scorePercent(attempt.getScorePrecent())
               .totalPoints(attempt.getTotalPoints())
               .submittedAt(attempt.getSubmitAt())
               .canRetake(canRetake)
               .aiReview(isAiReview)
               .bestScore(bestScore)
               .answers(answerResult)
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

    // student lamf bai
    public Map<String, Object> startQuiz(Long quizId,User student){
        Quiz quiz=quizRepository.findById(quizId)
                .orElseThrow(()->new AppException(ErrorCode.NOT_FOUND_QUIZ));
        validateQuizAvailable(quiz);
        validateCanAttempt(quiz,student);
        int numAttempt=quizAttemptRepository.countByQuizIdAndUserId(quizId,student.getId())+1;
        QuizAttempt quizAttempt=QuizAttempt.builder()
                .quiz(quiz)
                .user(student)
                .attemptNumber(numAttempt)
                .status(AttemptStatus.IN_PROGRESS)
                .build();
        quizAttempt=quizAttemptRepository.save(quizAttempt);

        AttemptDetail attemptDetail=AttemptDetail.builder()
                .attemptId(quizAttempt.getId())
                .quizId(quizId)
                .userId(student.getId())
                .createAt(Instant.now())
                .build();
        attemptDetail = attemptDetailRepository.save(attemptDetail);
        quizAttempt.setDetailQuizId(attemptDetail.getId());
        quizAttemptRepository.save(quizAttempt);

        ContentQuiz contentQuiz= quizContentRepository.findByQuizId(quizId)
                .orElseThrow(()->new AppException(ErrorCode.NOT_FOUND_QUIZ));

        QuizDetailResponse quizDetail=toQuizDetailResponse(quiz,contentQuiz);
        return Map.of(
                "attemptId", quizAttempt.getId(),
                "quiz", quizDetail,
                "serverDeadline", quizAttempt.getStartAt().plusMinutes(quiz.getTime())
        );

    }

    private void validateQuizAvailable(Quiz quiz) {
        if (quiz.getStatus() != QuizStatus.ACTIVE && quiz.getStatus() != QuizStatus.PUBLIC) {
            throw new AppException(ErrorCode.NOT_FOUND_QUIZ);
        }
        LocalDateTime now = LocalDateTime.now();
        if (quiz.getStartAt() != null && now.isBefore(quiz.getStartAt())) {
            throw new AppException(ErrorCode.NOT_FOUND_QUIZ);
        }
        if (quiz.getEndAt() != null && now.isAfter(quiz.getEndAt())) {
            throw new AppException(ErrorCode.NOT_FOUND_QUIZ);
        }
    }

    private void validateCanAttempt(Quiz quiz, User student) {
        // Kiểm tra đang làm dở chưa nộp
        quizAttemptRepository.findByQuizIdAndUserIdAndStatus(quiz.getId(), student.getId(), AttemptStatus.IN_PROGRESS)
                .ifPresent(a -> {
                    throw new AppException(ErrorCode.NOT_FOUND_QUIZ);
                });

        Optional<QuizAttempt> latest = quizAttemptRepository
                .findTopByQuizIdAndUserIdOrderByAttemptNumberDesc(quiz.getId(), student.getId());

        if (latest.isPresent()) {
            QuizAttempt last = latest.get();
            // Đã chọn AI review → khoá retake của người này
            if (last.isAiReviewRequest()) {
                throw new AppException(ErrorCode.NOT_FOUND_QUIZ);
            }
            // Hết lượt
            if (last.getAttemptNumber() >= quiz.getMaxAttempt()) {
                throw new AppException(ErrorCode.NOT_FOUND_QUIZ);
            }
        }
    }

    private boolean canRetake(Quiz quiz, User student, QuizAttempt currentAttempt) {
        if (currentAttempt.isAiReviewRequest()) return false;
        if (currentAttempt.getAttemptNumber() >= quiz.getMaxAttempt()) return false;
        if (quiz.getEndAt() != null && LocalDateTime.now().isAfter(quiz.getEndAt())) return false;
        return true;
    }

    private QuizDetailResponse toQuizDetailResponse(Quiz quiz,ContentQuiz contentQuiz){
        List<QuestionResponse> questions= contentQuiz.getQuestions().stream()
                .sorted(Comparator.comparingInt(Question::getOrder))
                .map(q->{
                    List<OptionResponse> options=q.getOptions()!=null?
                            q.getOptions().stream()
                                    .map(o->OptionResponse.builder()
                                            .text(o.getText())
                                            .build()).toList()
                            :List.of();
                    return QuestionResponse.builder()
                            .id(q.getId())
                            .questionText(q.getQuestionText())
                            .type(q.getType())
                            .points(q.getPoint())
                            .explanation(q.getExplanation())
                            .mediaUrl(q.getExplanation())
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

    // nop bai + tra ve diem
    public AttemptResponse submitAttempt(Long attemptId, List<AnswerRequest> answers, User student, boolean autoSubmit){
        QuizAttempt attempt=quizAttemptRepository.findById(attemptId)
                .orElseThrow(()->new AppException(ErrorCode.NOT_FOUND_QUIZ));
        if(attempt.getUser().getId().equals(student.getId())){
            throw new AppException(ErrorCode.NOT_FOUND_QUIZ);
        }
        if (attempt.getStatus() != AttemptStatus.IN_PROGRESS) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        ContentQuiz contentQuiz=quizContentRepository.findByQuizId(attempt.getQuiz().getId())
                .orElseThrow(()->new AppException(ErrorCode.NO_PERMISSION));
        List<Question> questions=contentQuiz.getQuestions();

        // cham diem
        List<Answer> 
    }

}
