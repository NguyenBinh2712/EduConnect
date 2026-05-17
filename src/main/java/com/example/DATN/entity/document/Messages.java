package com.example.DATN.entity.document;

import com.example.DATN.entity.enums.MediaType;
import com.example.DATN.entity.enums.MessageStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Document(collection = "messages")

@CompoundIndex(name = "conv_time_idx", def = "{'conversationId':1,'timestamp':-1}")

@CompoundIndex(name = "conv_sender_idx", def = "{'conversationId':1,'senderId':1}")

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Messages {

    @Id
    String id;

    @Indexed
    Long conversationId;

    @Indexed
    Long senderId;

    String content;

    // Media
    List<MessageMedia> mediaList;

    @Indexed
    Instant timestamp = Instant.now();

    MessageStatus status = MessageStatus.SENT;

    @Builder.Default
    Set<Long> deletedFor = new HashSet<>();

    @Builder.Default
    Set<Long> seenBy = new HashSet<>();

    String replyToMessageId;

    @Indexed
    boolean isPending = false;

    boolean isEdited = false;

    // Người report spam
    @Builder.Default
    Set<Long> reportedBy = new HashSet<>();
}