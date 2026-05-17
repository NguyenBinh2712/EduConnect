package com.example.DATN.service;

import com.example.DATN.dto.CloudinaryResponse;
import com.example.DATN.dto.teacherApplication.TeacherRequest;
import com.example.DATN.dto.teacherApplication.TeacherResponse;
import com.example.DATN.entity.Role;
import com.example.DATN.entity.TeacherApplication;
import com.example.DATN.entity.User;
import com.example.DATN.entity.enums.ApplicationStatus;
import com.example.DATN.exception.AppException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.repository.RoleRepository;
import com.example.DATN.repository.TeacherApplicationRepository;
import com.example.DATN.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.ArrayList;

import java.util.List;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Transactional
public class TeacherApplicationService {
    TeacherApplicationRepository applicationRepository;
    UserRepository userRepository;
    RoleRepository roleRepository;
    UploadService uploadService;
    ObjectMapper objectMapper;

    public TeacherResponse apply(Long userId,TeacherRequest request){
        User user=userRepository.findById(userId)
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_EXISTED));

        if (applicationRepository.existsByApplicantIdAndStatusIn(
                userId, List.of(ApplicationStatus.PENDING, ApplicationStatus.REVIEWING))) {
            throw new AppException(ErrorCode.ALREADY_HAS_PENDING_APPLICATION);
        }
        String idFrontUrl = null;
        String idBackUrl = null;
        String cvUrl = null;
        List<String> degreeUrls = new ArrayList<>();

        try {
            if (request.getIdCardFront() != null && !request.getIdCardFront().isEmpty()) {
                CloudinaryResponse res = uploadService.uploadFile(
                        request.getIdCardFront(), "teacher/applications/id", "front_" + userId);
                idFrontUrl = res.getUrl();
            }

            if (request.getIdCardBack() != null && !request.getIdCardBack().isEmpty()) {
                CloudinaryResponse res = uploadService.uploadFile(
                        request.getIdCardBack(), "teacher/applications/id", "back_" + userId);
                idBackUrl = res.getUrl();
            }

            if (request.getCv() != null && !request.getCv().isEmpty()) {
                CloudinaryResponse res = uploadService.uploadFile(
                        request.getCv(), "teacher/applications/cv", "cv_" + userId);
                cvUrl = res.getUrl();
            }

            if (request.getDegrees() != null && !request.getDegrees().isEmpty()) {
                for (MultipartFile degree : request.getDegrees()) {
                    if (!degree.isEmpty()) {
                        CloudinaryResponse res = uploadService.uploadFile(
                                degree, "teacher/applications/degrees", "degree_" + userId);
                        degreeUrls.add(res.getUrl());
                    }
                }
            }
        }catch (Exception e){
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
        String degreeUrlsJson = null;
        try {
            if (!degreeUrls.isEmpty()) {
                degreeUrlsJson = objectMapper.writeValueAsString(degreeUrls);
            }
        } catch (Exception e) {
            throw new AppException(ErrorCode.JSON_PROCESSING_ERROR);
        }

        TeacherApplication application = TeacherApplication.builder()
                .applicant(user)
                .reason(request.getReason())
                .idCardFrontUrl(idFrontUrl)
                .idCardBackUrl(idBackUrl)
                .degreeUrlsJson(degreeUrlsJson)
                .cvUrl(cvUrl)
                .status(ApplicationStatus.PENDING)
                .appliedAt(LocalDateTime.now())
                .build();
        application = applicationRepository.save(application);

        return mapToResponse(application);
    }
    private TeacherResponse mapToResponse(TeacherApplication app) {
        return TeacherResponse.builder()
                .id(app.getId())
                .applicantId(app.getApplicant().getId())
                .applicantEmail(app.getApplicant().getEmail())
                .reason(app.getReason())
                .idCardFrontUrl(app.getIdCardFrontUrl())
                .idCardBackUrl(app.getIdCardBackUrl())
                .degreeUrlsJson(app.getDegreeUrlsJson())
                .cvUrl(app.getCvUrl())
                .status(app.getStatus())
                .appliedAt(app.getAppliedAt())
                .reviewedAt(app.getReviewedAt())
                .reviewNote(app.getReviewNote())
                .build();
    }


    @PreAuthorize("hasRole('ADMIN')")
    public TeacherResponse reviewApplication(Long applicationId, ApplicationStatus newStatus, String reviewNote) {
        TeacherApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new AppException(ErrorCode.APPLICATION_NOT_FOUND));

        if (app.getStatus() != ApplicationStatus.PENDING) {
            throw new AppException(ErrorCode.APPLICATION_ALREADY_PROCESSED);
        }

        app.setStatus(newStatus);
        app.setReviewedAt(LocalDateTime.now());
        app.setReviewNote(reviewNote);

        if (newStatus == ApplicationStatus.APPROVED) {
            User user = app.getApplicant();
            var roles=user.getRoles();
            roles.add(Role.builder().name("TEACHER").build());
            user.setTeacher(true);
            user.setTeacherVerifiedAt(LocalDateTime.now());
            userRepository.save(user);
        }

        app = applicationRepository.save(app);
        return mapToResponse(app);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<TeacherResponse> getPending() {
        return applicationRepository.findByStatusOrderByAppliedAtDesc(ApplicationStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public TeacherResponse getMyApplication(Long userId) {
        TeacherApplication app = applicationRepository.findByApplicantIdAndStatus(userId, ApplicationStatus.PENDING)
                .orElseGet(() -> applicationRepository.findByApplicantIdAndStatus(userId, ApplicationStatus.REVIEWING)
                        .orElseThrow(() -> new AppException(ErrorCode.NO_PENDING_APPLICATION)));

        return mapToResponse(app);
    }

}
