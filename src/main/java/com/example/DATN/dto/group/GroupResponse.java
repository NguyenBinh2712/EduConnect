package com.example.DATN.dto.group;

import com.example.DATN.entity.enums.GroupPrivacy;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder @NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupResponse {
    Long id;
    String name;
    String description;
    String coverImageUrl;
    GroupPrivacy privacy;
    Long ownerId;
    String ownerName;
    long memberCount;
    LocalDateTime createdAt;
}