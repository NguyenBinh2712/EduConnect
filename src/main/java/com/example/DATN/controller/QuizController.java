package com.example.DATN.controller;

import com.example.DATN.dto.ApiResponse;
import com.example.DATN.dto.quiz.*;
import com.example.DATN.service.QuizService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QuizController {

    QuizService quizService;

    private Long getUserId(Jwt jwt) {
        return ((Number) jwt.getClaim("userId")).longValue();
    }

    //  TEACHER
    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping
    public ApiResponse<QuizResponse> createQuiz(
            @Valid @RequestBody QuizCreateRequest req,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ApiResponse<QuizResponse> res = new ApiResponse<>();
        res.setResult(quizService.createQuiz(req, getUserId(jwt)));
        return res;
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PutMapping("/{quizId}")
    public ApiResponse<QuizResponse> updateQuiz(
            @PathVariable Long quizId,
            @Valid @RequestBody QuizCreateRequest req,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ApiResponse<QuizResponse> res = new ApiResponse<>();
        res.setResult(quizService.updateQuiz(quizId, req, getUserId(jwt)));
        return res;
    }

    /** Kích hoạt quiz nhóm (GROUP → ACTIVE), không cần admin duyệt. */
    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/{quizId}/activate")
    public ApiResponse<QuizResponse> activateGroupQuiz(
            @PathVariable Long quizId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ApiResponse<QuizResponse> res = new ApiResponse<>();
        res.setResult(quizService.activateGroupQuiz(quizId, getUserId(jwt)));
        return res;
    }

    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/{quizId}/submissions")
    public ApiResponse<Page<AttemptResponse>> getSubmissions(
            @PathVariable Long quizId,
            Pageable pageable,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ApiResponse<Page<AttemptResponse>> res = new ApiResponse<>();
        res.setResult(quizService.getSubmissions(quizId, getUserId(jwt), pageable));
        return res;
    }

    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/submissions/{attemptId}")
    public ApiResponse<AttemptResponse> getSubmissionDetail(
            @PathVariable Long attemptId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ApiResponse<AttemptResponse> res = new ApiResponse<>();
        res.setResult(quizService.getSubmissionDetail(attemptId, getUserId(jwt)));
        return res;
    }

    @PreAuthorize("hasRole('TEACHER')")
    @PostMapping("/submissions/{attemptId}/feedback")
    public ApiResponse<FeedbackResponse> addFeedback(
            @PathVariable Long attemptId,
            @Valid @RequestBody TeacherFeedbackRequest req,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ApiResponse<FeedbackResponse> res = new ApiResponse<>();
        res.setResult(quizService.addFeedback(
                attemptId, req.getQuestionId(), req.getContent(), getUserId(jwt)));
        return res;
    }

    /** Danh sách quiz do teacher đang đăng nhập tạo. */
    @PreAuthorize("hasRole('TEACHER')")
    @GetMapping("/my-quizzes")
    public ApiResponse<List<QuizResponse>> getMyQuizzes(
            @AuthenticationPrincipal Jwt jwt
    ) {
        ApiResponse<List<QuizResponse>> res = new ApiResponse<>();
        res.setResult(quizService.getMyQuizzes(getUserId(jwt)));
        return res;
    }

    //  STUDENT
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/{quizId}/start")
    public ApiResponse<Map<String, Object>> startQuiz(
            @PathVariable Long quizId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ApiResponse<Map<String, Object>> res = new ApiResponse<>();
        res.setResult(quizService.startQuiz(quizId, getUserId(jwt)));
        return res;
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/attempts/{attemptId}/submit")
    public ApiResponse<AttemptResponse> submitAttempt(
            @PathVariable Long attemptId,
            @RequestBody List<AnswerRequest> answers,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ApiResponse<AttemptResponse> res = new ApiResponse<>();
        res.setResult(quizService.submitAttempt(attemptId, answers, getUserId(jwt), false));
        return res;
    }

    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/attempts/{attemptId}/ai-review")
    public ApiResponse<AiReviewResponse> aiReview(
            @PathVariable Long attemptId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ApiResponse<AiReviewResponse> res = new ApiResponse<>();
        res.setResult(quizService.aiReviewRequest(attemptId, getUserId(jwt)));
        return res;
    }

    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/{quizId}/my-attempts")
    public ApiResponse<List<AttemptResponse>> getMyAttempts(
            @PathVariable Long quizId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ApiResponse<List<AttemptResponse>> res = new ApiResponse<>();
        res.setResult(quizService.getMyAttempts(quizId, getUserId(jwt)));
        return res;
    }

    //  ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{quizId}/review")
    public ApiResponse<QuizResponse> reviewQuiz(
            @PathVariable Long quizId,
            @RequestBody ReviewQuizRequest req
    ) {
        ApiResponse<QuizResponse> res = new ApiResponse<>();
        res.setResult(quizService.reviewQuiz(quizId, req.isApproved(), req.getNote()));
        return res;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/pending")
    public ApiResponse<Page<QuizResponse>> getPendingQuizzes(Pageable pageable) {
        ApiResponse<Page<QuizResponse>> res = new ApiResponse<>();
        res.setResult(quizService.getPendingQuizzes(pageable));
        return res;
    }

    //  PUBLIC
    @GetMapping("/public")
    public ApiResponse<Slice<QuizResponse>> getPublicQuizzes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        ApiResponse<Slice<QuizResponse>> res = new ApiResponse<>();
        res.setResult(quizService.getPublicQuizzes(page, size));
        return res;
    }

    @GetMapping("/group/{groupId}")
    public ApiResponse<List<QuizResponse>> getQuizzesByGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        ApiResponse<List<QuizResponse>> res = new ApiResponse<>();
        res.setResult(quizService.getQuizzesByGroup(groupId, getUserId(jwt)));
        return res;
    }
}