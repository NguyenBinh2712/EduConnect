package com.example.DATN.entity;

import com.example.DATN.entity.enums.MediaType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PostMedia {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;

    @Enumerated(EnumType.STRING)
    private MediaType mediaType;
    private String publicId;
    private String url;           // Cloudinary / S3 URL
    private String thumbnail;     // chỉ video
    private Integer duration;     // video/voice (giây)
    private Integer sortOrder;
}