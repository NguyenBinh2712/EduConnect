package com.example.DATN.service;

import com.example.DATN.dto.CloudinaryResponse;
import com.example.DATN.dto.active.OtpRequest;
import com.example.DATN.dto.active.ResendOtpRequest;
import com.example.DATN.dto.user.*;
import com.example.DATN.entity.ActiveCode;
import com.example.DATN.entity.Profile;
import com.example.DATN.entity.Role;
import com.example.DATN.entity.User;
import com.example.DATN.entity.enums.OtpType;
import com.example.DATN.exception.AppException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.repository.ActiveCodeRepository;
import com.example.DATN.repository.ProfileRepository;
import com.example.DATN.repository.RoleRepository;
import com.example.DATN.repository.UserRepository;
import com.example.DATN.util.FileUploadUtil;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
@Transactional
public class UserService {
    UserRepository userRepository;
    MailService mailService;
    UploadService uploadService;
    ActiveCodeRepository activeCodeRepository;
    ProfileRepository profileRepository;
    RoleRepository roleRepository;

    PasswordEncoder passwordEncoder;

    public UserResponse registerUser(UserCreateRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
            throw new AppException(ErrorCode.USER_EXISTED);
        }
        Role role=roleRepository.findByName("STUDENT")
                .orElseThrow(()->new AppException(ErrorCode.ROLE_NOT_EXISTED));

        User user= new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setActive(false);
        user.setStatus(false);
        user.setCreateAt(LocalDate.now());
        user.setRoles(Set.of(role));
        user.setProfile(null);

        String otp=generateOtp();
        ActiveCode activeCode=ActiveCode.builder()
                .otp(otp)
                .createAt(Instant.now())
                .exp(Instant.now().plus(60, ChronoUnit.SECONDS))
                .type(OtpType.REGISTER)
                .user(user)
                .build();

        user.setActiveCodes(new HashSet<>(Set.of(activeCode)));
        userRepository.save(user);

        mailService.sendEmail(
                user.getEmail(),
                "Xác thực tài khoản",
                "Mã xác thực của bạn là: " + otp
        );

        return toUserResponse(user);

    }

    public void createOrUpdateProfile(ProfileRequest request){
        User user=getCurrentUser();
        Profile profile=user.getProfile();
        if(profile==null){
             profile=new Profile();
             profile.setUser(user);
        }

        profile.setFullName(request.getFullName());
        profile.setBirth(request.getBirth());
        profile.setAvatarUrl(request.getAvatar());
        profile.setAvatarPublicId(request.getAvatarPublicId());
        profile.setGender(request.getGender());
        profile.setBio(request.getBio());

        profileRepository.save(profile);
        user.setProfile(profile);
        userRepository.save(user);
    }

    public void changeAvatar(MultipartFile file){
        User user=getCurrentUser();
        FileUploadUtil.assertAllowed(file);
        String fileName=FileUploadUtil.getFileName(file.getOriginalFilename());
        CloudinaryResponse response=uploadService.uploadFile(file, "avatar",fileName);

        Profile profile = user.getProfile();
        if (profile == null) {
            profile = new Profile();
            user.setProfile(profile);
            profile.setUser(user);
        }

        if (profile.getAvatarPublicId() != null) {
            uploadService.deleteMedia(profile.getAvatarPublicId());
        }

        profile.setAvatarUrl(response.getUrl());
        profile.setAvatarPublicId(response.getPublicId());
        userRepository.save(user);
    }

    public void changeMyPassword(ChangePasswordRequest request) {
        User user = getCurrentUser();

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.INCORRECT_PASSWORD);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public void verifyOtp(OtpRequest request) {
        User user = userRepository.findUserByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        ActiveCode activeCode = activeCodeRepository.findByUserAndOtpAndType(user, request.getOtp(), OtpType.REGISTER)
                .orElseThrow(()->new AppException(ErrorCode.INVALID_OTP));


        if (activeCode.getExp().isBefore(Instant.now())) {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        user.setActive(true);
        activeCodeRepository.delete(activeCode);
        userRepository.save(user);
    }

    public void resendOtp(ResendOtpRequest request) {

        User user = userRepository.findUserByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        activeCodeRepository.deleteByUserAndType(user, request.getType());

        String otp = generateOtp();

        ActiveCode activeCode = ActiveCode.builder()
                .otp(otp)
                .createAt(Instant.now())
                .exp(Instant.now().plus(60, ChronoUnit.SECONDS))
                .user(user)
                .type(request.getType())
                .build();

        activeCodeRepository.save(activeCode);

        mailService.sendEmail(
                user.getEmail(),
                "Xác thực tài khoản",
                "Mã xác thực của bạn là: " + otp + "\nHiệu lực trong 60 giây."
        );
    }
    public void requestForgotPasswordOtp(String email) {
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        activeCodeRepository.deleteByUserAndType(user,OtpType.FORGOT_PASSWORD);

        String otp = generateOtp();
        ActiveCode activeCode = ActiveCode.builder()
                .otp(otp)
                .createAt(Instant.now())
                .exp(Instant.now().plus(60, ChronoUnit.SECONDS))
                .user(user)
                .type(OtpType.FORGOT_PASSWORD)
                .build();

        user.getActiveCodes().add(activeCode);
        userRepository.save(user);

        mailService.sendEmail(
                user.getEmail(),
                "Yêu cầu đặt lại mật khẩu",
                "Mã xác thực để đặt lại mật khẩu: " + otp + "\nHiệu lực trong 60 giây."
        );
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findUserByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        ActiveCode activeCode = activeCodeRepository.findByUserAndOtpAndType(user, request.getOtp(), OtpType.FORGOT_PASSWORD)
                .orElseThrow(()->new AppException(ErrorCode.INVALID_OTP));

        if (activeCode.getExp().isBefore(Instant.now())) {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        activeCodeRepository.delete(activeCode);
        userRepository.save(user);

        mailService.sendEmail(
                request.getEmail(),
                "Thông báo thay đổi mật khẩu",
                "Mật khẩu đã được cập nhật thành công. Hãy đăng nhập lại."
        );
    }

    public UserResponse getMyInfo() {
        User user = getCurrentUser();
        return toUserResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUser() {
        return userRepository.findAll().stream()
                .map(this::toUserResponse)
                .toList();
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(long id) {
        userRepository.deleteById(id);
    }

    private String generateOtp(){
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    private User getCurrentUser(){
        String email= SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findUserByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }
    public UserResponse getUserById(long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        return toUserResponse(user);
    }

    private UserResponse toUserResponse(User user){
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .status(user.isStatus())
                .createAt(user.getCreateAt())
                .profile(toProfileResponse(user.getProfile()))
                .build();
    }

    private ProfileResponse toProfileResponse(Profile profile){
        if (profile == null) return null;

        return ProfileResponse.builder()
                .id(profile.getId())
                .fullName(profile.getFullName())
                .bio(profile.getBio())
                .birth(profile.getBirth())
                .gender(profile.getGender())
                .avatarUrl(profile.getAvatarUrl())
                .avatarPublicId(profile.getAvatarPublicId())
                .build();
    }

}
