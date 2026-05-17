package com.example.DATN.util;

import com.example.DATN.exception.AppException;
import com.example.DATN.exception.ErrorCode;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class FileUploadUtil {

    // Tăng lên 10MB để phù hợp với CV PDF, ảnh CMND/bằng cấp chất lượng
    public static final long MAX_FILE_SIZE = 100 * 1024 * 1024L; // 10MB

    // Pattern mặc định (ảnh + pdf) – dùng khi không override
    public static final String IMAGE_AND_PDF_PATTERN =
            "^[^\\s]+\\.(jpg|jpeg|png|gif|bmp|pdf)$";

    // Danh sách extension mặc định
    private static final List<String> DEFAULT_ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "bmp", "pdf"
    );

    // Danh sách content-type an toàn
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/bmp",
            "application/pdf"
    );

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss").withLocale(Locale.ROOT);

    public static void assertAllowed(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new AppException(ErrorCode.FILE_EMPTY);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException(
                    "Kích thước file vượt quá giới hạn 10MB");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase(Locale.ROOT).matches(IMAGE_AND_PDF_PATTERN)) {
            throw new RuntimeException(
                    "Chỉ chấp nhận file: jpg, jpeg, png, gif, bmp, pdf");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new RuntimeException("Loại file không được hỗ trợ");
        }
    }

    public static String getFileName(String name) {
        String date = LocalDateTime.now().format(FORMATTER);
        String uuidShort = UUID.randomUUID().toString().substring(0, 8);
        return String.format("%s_%s_%s", name, uuidShort, date);
    }

    // (Tùy chọn) Nếu muốn dùng ở nơi cần thêm extension vào tên file
    public static String getFileNameWithExtension(String prefix, MultipartFile file) {
        String extension = getFileExtension(file.getOriginalFilename());
        return getFileName(prefix) + (extension.isEmpty() ? "" : "." + extension);
    }

    private static String getFileExtension(String filename) {
        if (filename == null) return "";
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1 || dotIndex == filename.length() - 1) ? "" : filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}