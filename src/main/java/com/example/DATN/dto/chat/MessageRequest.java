package com.example.DATN.dto.chat;

import com.example.DATN.entity.enums.MediaType;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Setter
@Getter
public class MessageRequest {
    Long conversationId;
    String content;
    List<MultipartFile> files;
    MediaType type;
}
