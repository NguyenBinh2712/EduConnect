package com.example.DATN.dto;

import com.example.DATN.entity.enums.MediaType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Medias {
    String url;
    String publicId;
    String thumbnail;
    Integer duration;
    MediaType mediaType;
    Integer sortOrder;
}
