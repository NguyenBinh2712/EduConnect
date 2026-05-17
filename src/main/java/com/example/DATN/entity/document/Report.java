package com.example.DATN.entity.document;

import com.example.DATN.entity.enums.ChatReportType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
@Document("reports")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Report {
    private String id;
    private Long reporterId;
    private String messageId;
    private Long conversationId;
    private ChatReportType type;
    private String reason;
    private Instant createdAt;
}
