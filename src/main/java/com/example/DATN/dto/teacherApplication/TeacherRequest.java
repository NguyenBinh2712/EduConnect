package com.example.DATN.dto.teacherApplication;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TeacherRequest {
    @NotBlank(message = "Lý do không được để trống")
    private String reason;
    private MultipartFile idCardFront;
    private MultipartFile idCardBack;
    private List<MultipartFile> degrees;
    private MultipartFile cv;
    private List<String> subject;

}
