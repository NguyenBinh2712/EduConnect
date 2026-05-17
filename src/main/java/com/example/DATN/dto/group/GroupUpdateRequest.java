package com.example.DATN.dto.group;

import com.example.DATN.entity.enums.GroupPrivacy;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupUpdateRequest {
    private String name;
    private String description;
    private String coverImageUrl;
    private GroupPrivacy privacy;
}