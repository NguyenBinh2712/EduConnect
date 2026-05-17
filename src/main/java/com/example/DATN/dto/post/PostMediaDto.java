package com.example.DATN.dto.post;

import com.example.DATN.entity.enums.MediaType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostMediaDto {
    private Long id;
    private MediaType mediaType;
    private String url;
    private String thumbnail;
    private Integer duration;
    private Integer sortOrder;

}
