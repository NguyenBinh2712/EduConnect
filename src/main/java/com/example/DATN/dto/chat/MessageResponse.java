package com.example.DATN.dto.chat;

import com.example.DATN.entity.document.MessageMedia;
import com.example.DATN.entity.enums.MediaType;
import com.example.DATN.entity.enums.MessageStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Setter
@Getter
public class MessageResponse {
     String id;
     Long conversationId;
     Long senderId;
     String content;
     List<MessageMedia> messageMedias;
     Instant timestamp;
     MessageStatus status;
     Set<Long> seenBy;
     boolean isPending;
//     boolean isSpam;
    boolean isEdited;
//    boolean isDeleted;
}

