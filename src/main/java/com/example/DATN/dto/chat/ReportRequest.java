package com.example.DATN.dto.chat;

import com.example.DATN.entity.enums.ChatReportType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Setter
@Getter
public class ReportRequest {
    Long convId;
    ChatReportType type;
    String reason;
}

