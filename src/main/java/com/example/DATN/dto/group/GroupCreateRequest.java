package com.example.DATN.dto.group;

import com.example.DATN.entity.enums.GroupPrivacy;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GroupCreateRequest {
    String name;
    String description;
    String coverImageUrl;
    GroupPrivacy privacy;
    List<String> subjectCode;
}
