package com.example.DATN.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.DATN.dto.CloudinaryResponse;
import com.example.DATN.dto.Medias;
import com.example.DATN.entity.PostMedia;
import com.example.DATN.entity.enums.MediaType;
import com.example.DATN.exception.AppException;
import com.example.DATN.exception.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UploadService {

    Cloudinary cloudinary;

    public CloudinaryResponse uploadFile(MultipartFile file, String folder, String publicIdPrefix) {
        try {
            if (file.isEmpty()) {
                throw new AppException(ErrorCode.FILE_EMPTY);
            }

            String publicId = generatePublicId(folder, publicIdPrefix);

            Map options = ObjectUtils.asMap(
                    "public_id", publicId,
                    "resource_type", "auto",
                    "folder", folder,
                    "overwrite", true
            );

            Map result = cloudinary.uploader().uploadLarge(
                    file.getInputStream(),
                    options
            );

            return CloudinaryResponse.builder()
                    .publicId((String) result.get("public_id"))
                    .url((String) result.get("secure_url"))
                    .build();

        } catch (IOException e) {
            throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    public List<Medias> uploadMedias(
            List<MultipartFile> files,
            String targetType,
            String targetId
    ) {

        List<Medias> mediaList = new ArrayList<>();
        String folder = targetType + "/" + targetId;

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            if (file.isEmpty()) continue;

            try {
                String contentType = Optional.ofNullable(file.getContentType())
                        .map(String::toLowerCase)
                        .orElse("");

                boolean isImage = contentType.startsWith("image/");
                boolean isVideo = contentType.startsWith("video/");
                boolean isAudio = contentType.startsWith("audio/");

                boolean isVideoOrAudio = isVideo || isAudio;

                String publicId = folder + "/media_" + i + "_" + System.currentTimeMillis();

                byte[] fileBytes = file.getBytes();

                Map options;

                if (isVideoOrAudio) {
                    List<Map> eager = isVideo ? List.of(
                            ObjectUtils.asMap(
                                    "width", 400,
                                    "height", 300,
                                    "crop", "thumb",
                                    "gravity", "auto",
                                    "format", "jpg"
                            )
                    ) : null;

                    options = ObjectUtils.asMap(
                            "public_id", publicId,
                            "resource_type", "video", // ✅ dùng cho cả audio
                            "folder", folder,
                            "eager", eager,
                            "eager_async", false,
                            "overwrite", true
                    );

                } else {
                    options = ObjectUtils.asMap(
                            "public_id", publicId,
                            "resource_type", "image",
                            "folder", folder,
                            "overwrite", true
                    );
                }

                Map result = cloudinary.uploader().upload(fileBytes, options);

                String url = (String) result.get("secure_url");
                String uploadedPublicId = (String) result.get("public_id");

                MediaType mediaType = determineMediaType(contentType);

                Integer duration = null;
                if (isVideoOrAudio) {
                    Object raw = result.get("duration");
                    if (raw instanceof Number) {
                        duration = ((Number) raw).intValue();
                    }
                }

                String thumbnail = extractThumbnailUrl(result);

                if (thumbnail == null) {
                    thumbnail = isAudio
                            ? "https://res.cloudinary.com/demo/image/upload/v1/default_audio.png"
                            : url;
                }

                Medias media = Medias.builder()
                        .mediaType(mediaType)
                        .url(url)
                        .publicId(uploadedPublicId)
                        .thumbnail(thumbnail)
                        .duration(duration)
                        .sortOrder(i)
                        .build();

                mediaList.add(media);

            } catch (IOException e) {
                throw new AppException(ErrorCode.FILE_UPLOAD_FAILED);
            }
        }

        return mediaList;
    }

    public void deleteMedia(String publicId) {
        if (publicId == null || publicId.trim().isEmpty()) return;

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            System.err.println("Không thể xóa media Cloudinary: " + publicId + " - " + e.getMessage());
        }
    }

    public void deleteMedias(List<String> publicIds) {
        if (publicIds == null || publicIds.isEmpty()) return;

        try {
            cloudinary.api().deleteResources(
                    publicIds,
                    ObjectUtils.asMap("resource_type", "all")
            );
        } catch (Exception e) {
            System.err.println("Xóa batch media Cloudinary thất bại: " + e.getMessage());
        }
    }

    private String generatePublicId(String folder, String prefix) {
        return folder + "/" + prefix + "_" + System.currentTimeMillis();
    }

    private MediaType determineMediaType(String contentType) {
        if (contentType.startsWith("image/")) return MediaType.IMAGE;
        if (contentType.startsWith("video/")) return MediaType.VIDEO;
        if (contentType.startsWith("audio/")) return MediaType.AUDIO;

        return MediaType.OTHER;
    }

    private String extractThumbnailUrl(Map result) {
        if (result.containsKey("eager")) {
            List<Map> eagerList = (List<Map>) result.get("eager");
            if (!eagerList.isEmpty()) {
                return (String) eagerList.get(0).get("secure_url");
            }
        }
        return null;
    }
}