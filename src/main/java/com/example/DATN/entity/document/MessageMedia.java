package com.example.DATN.entity.document;

import com.example.DATN.entity.enums.MediaType;
import lombok.*;
import lombok.experimental.FieldDefaults;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
// embedded object
public class MessageMedia {
    String url;
    String publicId;
    MediaType mediaType;
    String thumbnail;
    Integer duration;
}
