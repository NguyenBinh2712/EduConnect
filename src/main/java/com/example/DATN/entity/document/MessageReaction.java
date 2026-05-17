package com.example.DATN.entity.document;

import com.example.DATN.entity.enums.ReactionType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("messageReaction")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessageReaction {
    String id;
    String messageId;
    Long userId;
    ReactionType type;
}
