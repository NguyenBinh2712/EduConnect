package com.example.DATN.entity.enums;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum ReportReason {

    SPAM("Spam"),
    SENSITIVE_CONTENT("Nội dung nhạy cảm"),
    VIOLENCE("Bạo lực"),
    HARASSMENT("Quấy rối / Bắt nạt"),
    FALSE_INFORMATION("Thông tin sai lệch"),
    HATE_SPEECH("Ngôn từ thù ghét"),
    SCAM("Lừa đảo"),
    OTHER("Khác");

    private final String displayName;

    ReportReason(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}