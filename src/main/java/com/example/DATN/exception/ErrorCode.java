package com.example.DATN.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    USER_EXISTED(1001,"USER EXISTED", HttpStatus.CONFLICT),
    UNCATEGORIZED_EXCEPTION(9999,"UNCATEGORIZED EXCEPTION",HttpStatus.INTERNAL_SERVER_ERROR),
    PASSWORD_VALID(1002,"PASSWORD MUST BE AT LEAST 8 CHARACTERS",HttpStatus.BAD_REQUEST),
    INVALID_KEY(9000,"INVALID MESSAGES KEY",HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1003,"USER NOT EXISTED",HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1004,"UNAUTHENTICATED",HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1005,"You do not have permission",HttpStatus.FORBIDDEN),

    // Teacher
    ALREADY_TEACHER(1010,"Bạn đã là giáo viên rồi",HttpStatus.CONFLICT),
    ALREADY_APPLIED_TEACHER(1011,"Bạn đã có đơn đang chờ duyệt",HttpStatus.CONFLICT),
    APPLICATION_NOT_FOUND(1012,"Không tìm thấy đơn đăng ký",HttpStatus.NOT_FOUND),
    APPLICATION_ALREADY_REVIEWED(1013,"Đơn đã được xử lý",HttpStatus.CONFLICT),
    NO_PENDING_APPLICATION(1014,"Bạn chưa có đơn đăng ký nào đang chờ",HttpStatus.NOT_FOUND),

    // Role
    ROLE_EXISTED(1006,"ROLE EXISTED",HttpStatus.CONFLICT),
    ROLE_NOT_EXISTED(1007,"ROLE NOT EXISTED",HttpStatus.NOT_FOUND),

    DOB_VALID(1008,"You must be at least {min} years old.",HttpStatus.BAD_REQUEST),

    // Post
    POST_NOT_EXISTED(3001,"POST NOT EXISTED",HttpStatus.NOT_FOUND),
    COMMENT_NOT_EXISTED(3002,"COMMENT NOT EXISTED",HttpStatus.NOT_FOUND),
    FILE_UPLOAD_FAILED(3003,"File upload failed",HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_EMPTY(3004,"File empty",HttpStatus.BAD_REQUEST),
    ALREADY_REPORTED(3005,"Already reported",HttpStatus.CONFLICT),
    CANNOT_REPORT_OWN_CONTENT(3006,"Cannot report own content",HttpStatus.BAD_REQUEST),
    REPORT_NOT_FOUND(3007,"Report not found",HttpStatus.NOT_FOUND),
    REPORT_ALREADY_HANDLED(3008,"Report already handled",HttpStatus.CONFLICT),
    JSON_PROCESSING_ERROR(3009,"JSON processing error",HttpStatus.BAD_REQUEST),
    INVALID_FILE_NAME(3010,"Invalid file name",HttpStatus.BAD_REQUEST),
    INVALID_CONTENT_TYPE(3011,"Invalid content type", HttpStatus.BAD_REQUEST),

    // Auth
    INVALID_OTP(9001,"OTP NOT VALID",HttpStatus.BAD_REQUEST),
    OTP_EXPIRED(9002,"OTP EXPIRED",HttpStatus.BAD_REQUEST),
    INCORRECT_PASSWORD(9003,"Incorrect password",HttpStatus.BAD_REQUEST),
    INVALID_TOKEN(9004,"INVALID TOKEN",HttpStatus.UNAUTHORIZED),
    INVALID_REQUEST(9005,"INVALID REQUEST",HttpStatus.BAD_REQUEST),
    // Application duplicate
    ALREADY_HAS_PENDING_APPLICATION(4001,"Already has pending application",HttpStatus.CONFLICT),
    APPLICATION_ALREADY_PROCESSED(4002,"Application already processed",HttpStatus.CONFLICT),

    // Friend
    CANNOT_ADD_SELF(4003, "Không thể kết bạn chính mình",HttpStatus.BAD_REQUEST),
    FRIEND_REQUEST_NOT_FOUND(4004,"Friend request not found",HttpStatus.NOT_FOUND),
    ALREADY_FRIEND(4005,"Đã là bạn bè",HttpStatus.CONFLICT),
    FRIEND_REQUEST_ALREADY(4006,"Lời mời đã tồn tại",HttpStatus.CONFLICT),
    NO_PERMISSION_ACCEPT_REQUEST(4007, "Không có quyền chấp nhận lời mời",HttpStatus.FORBIDDEN),
    NO_PERMISSION_CANCEL_REQUEST(4008, "Không có quyền hủy lời mời",HttpStatus.FORBIDDEN),
    NO_PERMISSION_REJECTED_REQUEST(4009, "Không có quyền từ chối lời mời",HttpStatus.FORBIDDEN),

    // Group
    NO_PERMISSION_CREATE_GROUP(4010,"Chỉ giáo viên mới có quyền tạo group",HttpStatus.FORBIDDEN),
    GROUP_NOT_EXISTED(4011,"Group not existed",HttpStatus.NOT_FOUND),
    ALREADY_IN_GROUP(4012,"Đã ở trong nhóm",HttpStatus.CONFLICT),
    JOIN_REQUEST_ALREADY_SENT(4013,"Đã gửi yêu cầu tham gia",HttpStatus.CONFLICT),
    JOIN_REQUEST_NOT_FOUND(4014, "Không tìm thấy yêu cầu tham gia", HttpStatus.NOT_FOUND),
    REQUEST_ALREADY_PROCESSED(4015, "Yêu cầu đã được xử lý", HttpStatus.CONFLICT),
    GROUP_NOT_FOUND,


    NOT_IN_GROUP(4016, "Bạn không thuộc nhóm này", HttpStatus.FORBIDDEN),
    NOT_FRIENDS(4017, "Hai người không phải bạn bè", HttpStatus.BAD_REQUEST),
    CAN_ONLY_INVITE_FRIENDS(4018, "Chỉ có thể mời bạn bè", HttpStatus.BAD_REQUEST),

    OWNER_CANNOT_LEAVE(4019, "Chủ nhóm không thể rời nhóm", HttpStatus.BAD_REQUEST),
    CANNOT_REMOVE_SELF(4020, "Không thể tự xóa chính mình", HttpStatus.BAD_REQUEST),

    CANNOT_TRANSFER_OWNERSHIP_THIS_WAY(4021, "Không thể chuyển quyền owner theo cách này", HttpStatus.BAD_REQUEST),

    NO_PERMISSION_TO_VIEW_GROUP(4022, "Không có quyền xem nhóm", HttpStatus.FORBIDDEN),
    NO_PERMISSION(4023, "Không có quyền ", HttpStatus.FORBIDDEN),
    POST_NOT_IN_GROUP(4024, "Post khong tồn tại trong group ", HttpStatus.FORBIDDEN),


    CONVERSATION_NOT_FOUND(5000," converdation not found",HttpStatus.NOT_FOUND),
    CANNOT_CHAT_WITH_SELF,
    BLOCK_NOT_FOUND,
    NOT_CONVERSATION_MEMBER,
    CANNOT_REMOVE_OWNER,
    BLOCKED_USER,
    MESSAGE_NOT_FOUND,

    NOT_FOUND_QUIZ(1050, "Quiz content không tìm thấy", HttpStatus.NOT_FOUND),
    QUIZ_CONTENT_NOT_FOUND(1050, "Quiz content không tìm thấy", HttpStatus.NOT_FOUND),
    QUIZ_NOT_ACTIVE(1051, "Quiz chưa được kích hoạt", HttpStatus.BAD_REQUEST),
    QUIZ_EXPIRED(1052, "Quiz đã hết hạn làm bài", HttpStatus.BAD_REQUEST),
    QUIZ_NOT_STARTED(1053, "Quiz chưa đến thời gian mở", HttpStatus.BAD_REQUEST),
    ATTEMPT_IN_PROGRESS(1054, "Bạn đang có bài làm dở, vui lòng nộp trước", HttpStatus.BAD_REQUEST),
    MAX_ATTEMPT_REACHED(1055, "Bạn đã hết lượt làm bài", HttpStatus.BAD_REQUEST),
    AI_REVIEW_LOCKED(1056, "Đã dùng AI review, không thể làm lại", HttpStatus.BAD_REQUEST),


    ;
    private int code;
    private String messages;
    private HttpStatus httpStatus;

    ErrorCode(int code, String messages, HttpStatus httpStatus) {
        this.code = code;
        this.messages = messages;
        this.httpStatus=httpStatus;
    }

    ErrorCode() {
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessages() {
        return messages;
    }

    public void setMessages(String messages) {
        this.messages = messages;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

}
