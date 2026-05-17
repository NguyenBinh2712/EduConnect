package com.example.DATN.dto.group;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data @Builder @NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InviteRequest {
    Long friendId;
}
