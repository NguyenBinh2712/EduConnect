package com.example.DATN.service;

import com.example.DATN.dto.Medias;
import com.example.DATN.dto.post.*;
import com.example.DATN.entity.*;
import com.example.DATN.entity.enums.*;
import com.example.DATN.exception.AppException;
import com.example.DATN.exception.ErrorCode;
import com.example.DATN.repository.*;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostService {

    UploadService uploadService;
    PostRepository postRepository;
    PostMediaRepository postMediaRepository;
    CommentRepository commentRepository;
    ReactionRepository reactionRepository;
    UserRepository userRepository;
    ReportRepository reportRepository;
    MailService mailService;
    GroupRepository groupRepository;
    GroupMembershipRepository groupMembershipRepository;

    NotificationService notificationService;

    private User findUserById(Long userId){
        return userRepository.findById(userId)
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_EXISTED));
    }
    public PostResponse createPost(
            Long userId,
            PostCreateRequest request,
            List<MultipartFile> files){

        Post post = new Post();
        post.setUser(findUserById(userId));
        if (request.getGroupId() != null) {
            Group group = groupRepository.findById(request.getGroupId())
                    .orElseThrow(() -> new AppException(ErrorCode.GROUP_NOT_EXISTED));
            boolean isMember = groupMembershipRepository
                    .existsByGroupIdAndUserId(request.getGroupId(), userId);
            if (!isMember) throw new AppException(ErrorCode.NOT_IN_GROUP);
            post.setGroup(group);
        }
        post.setContent(request.getContent());
        post.setPrivacy(request.getPrivacy());
        post.setOriginalPostId(request.getOriginalPostId());
        post.setHidden(false);

        postRepository.save(post);

        if (files != null && !files.isEmpty()) {
            List<Medias> medias = uploadService.uploadMedias(files,"posts",String.valueOf(post.getId()));
            List<PostMedia> postMedias=toPostMedia(medias);
            postMedias.forEach(m -> m.setPost(post));
            postMediaRepository.saveAll(postMedias);
            post.setMedias(postMedias);
            postRepository.save(post);
        }
        return mapToResponse(post);
    }
 
    public PostResponse updatePost(
            Long userId,
            Long postId,
            PostCreateRequest request) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));

        if(!post.getUser().getId().equals(userId))
            throw new AppException(ErrorCode.UNAUTHORIZED);

        post.setContent(request.getContent());
        post.setPrivacy(request.getPrivacy());

        postRepository.save(post);

        return mapToResponse(post);
    }
    public List<PostMedia> toPostMedia(List<Medias> results) {
        List<PostMedia> list = new ArrayList<>();

        for (int i = 0; i < results.size(); i++) {
            Medias r = results.get(i);

            list.add(PostMedia.builder()
                    .url(r.getUrl())
                    .publicId(r.getPublicId())
                    .thumbnail(r.getThumbnail())
                    .duration(r.getDuration())
                    .mediaType(r.getMediaType())
                    .sortOrder(i)
                    .build());
        }

        return list;
    }


    public void deletePost(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));

        if (!post.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Xóa media trên Cloudinary + DB
        if (post.getMedias() != null && !post.getMedias().isEmpty()) {
            List<String> publicIds = post.getMedias().stream()
                    .map(PostMedia::getPublicId)
                    .filter(Objects::nonNull)
                    .toList();
            uploadService.deleteMedias(publicIds);
            postMediaRepository.deleteAll(post.getMedias());
        }

        // Xóa reactions
        reactionRepository.deleteByTargetIdAndTargetType(postId, ReactionTargetType.POST);

        // Xóa comments
        commentRepository.deleteByPostId(postId);

        postRepository.delete(post);
    }

    public PostResponse sharePost(Long userId, Long originalPostId) {
        Post original = postRepository.findById(originalPostId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));

        Post sharePost = new Post();
        sharePost.setUser(findUserById(userId));
        sharePost.setOriginalPostId(originalPostId);
        sharePost.setContent("");
        sharePost.setPrivacy(Privacy.PUBLIC);
        sharePost.setHidden(false);
        postRepository.save(sharePost);

        notificationService.sendNotify(
                original.getUser().getId(),
                userId,
                NotificationType.SHARE_POST,
                originalPostId.toString(),
                TargetType.POST
        );
        return mapToResponse(sharePost);
    }
 
    public Slice<PostResponse> getFeed(int page, int size) {

        Pageable pageable = PageRequest.of(page,size, Sort.by("createdAt").descending());

        Slice<Post> posts = postRepository
                .findByIsHiddenFalseAndPrivacyAndGroupIsNull(Privacy.PUBLIC, pageable);
        return posts.map(this::mapToResponse);
    }

    public PostResponse getPostDetail(Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));

        PostResponse response = mapToResponse(post);

        List<CommentResponse> comments =
                getCommentsByPost(postId);

        response.setComments(comments);

        return response;
    }

    public PostResponse mapToResponse(Post post) {

        List<PostMediaDto> mediaList =
                post.getMedias() == null
                        ? List.of()
                        : post.getMedias()
                        .stream()
                        .sorted((m1, m2) -> {
                            if (m1.getSortOrder() == null) return 1;
                            if (m2.getSortOrder() == null) return -1;
                            return m1.getSortOrder()
                                    .compareTo(m2.getSortOrder());
                        })
                        .map(media -> PostMediaDto.builder()
                                .id(media.getId())
                                .mediaType(media.getMediaType())
                                .url(media.getUrl())
                                .thumbnail(media.getThumbnail())
                                .duration(media.getDuration())
                                .sortOrder(media.getSortOrder())
                                .build())
                        .toList();

        Map<ReactionType, Long> reactions =
                reactionRepository
                        .findByTargetIdAndTargetType(
                                post.getId(),
                                ReactionTargetType.POST
                        )
                        .stream()
                        .collect(Collectors.groupingBy(
                                Reaction::getType,
                                Collectors.counting()
                        ));

        long commentCount =
                commentRepository.countByPostId(post.getId());

        return PostResponse.builder()
                .id(post.getId())
                .user(toUserPost(post.getUser()))
                .content(post.getContent())
                .privacy(post.getPrivacy())
                .postType(post.getPostType())
                .originalPostId(post.getOriginalPostId())
                .medias(mediaList)
                .reactions(reactions)
                .commentCount(commentCount)
                .isHidden(post.isHidden())
                .createdAt(post.getCreatedAt())
                .build();
    }

    private UserPost toUserPost(User user){
        return UserPost.builder()
                .id(user.getId())
                .fullName(user.getProfile().getFullName())
                .urlAvatar(user.getProfile().getAvatarUrl())
                .build();
    }
// comment

    public CommentResponse createComment(
            Long postId,
            CommentRequest request,
            Long userId){

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));

        userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Comment parent = null;

        if(request.getParentId() != null){
            parent = commentRepository.findById(request.getParentId()).orElse(null);
        }

        Comment comment = Comment.builder()
                .post(post)
                .userId(userId)
                .parent(parent)
                .content(request.getContent())
                .build();

        commentRepository.save(comment);

        if (parent == null) {
            notificationService.sendNotify(
                    post.getUser().getId(),
                    userId,
                    NotificationType.COMMENT_POST,
                    postId.toString(),
                    TargetType.POST
            );
        } else {
            notificationService.sendNotify(
                    parent.getUserId(),
                    userId,
                    NotificationType.REPLY_COMMENT,
                    postId.toString(),
                    TargetType.POST
            );
        }

        return mapToCommentResponse(comment);
    }

    public List<CommentResponse> getCommentsByPost(Long postId){

        return commentRepository.findByPostId(postId)
                .stream()
                .map(this::mapToCommentResponse)
                .toList();
    }

    public long countComment(Long postId){
        return commentRepository.countByPostId(postId);
    }

    private CommentResponse mapToCommentResponse(Comment comment){
        User user=userRepository.findById(comment.getUserId())
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_EXISTED));
        return CommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .userId(comment.getUserId())
                .fullName(user.getProfile().getFullName())
                .parentId(
                        comment.getParent() != null ?
                                comment.getParent().getId() : null
                )
                .createdAt(comment.getCreatedAt())
                .build();
    }

    // reaction
    public PostResponse reactToPost(Long postId, Long userId, ReactionRequest request){
        Post post=postRepository.findById(postId)
                .orElseThrow(()->new AppException(ErrorCode.POST_NOT_EXISTED));
        Optional<Reaction> existing = reactionRepository.findByTargetIdAndTargetTypeAndUserId(
                postId, ReactionTargetType.POST, userId);

        if(existing.isPresent()){
            Reaction reaction=existing.get();
            if(reaction.getType()==request.getType()){
                reactionRepository.delete(reaction);
            }else{
               reaction.setType(request.getType());
               reactionRepository.save(reaction);
            }
        }else {
            Reaction reaction=Reaction.builder()
                    .targetId(postId)
                    .targetType(ReactionTargetType.POST)
                    .userId(userId)
                    .type(request.getType())
                    .build();
            reactionRepository.save(reaction);
        }
        if(existing.isEmpty()){
            notificationService.sendNotify(
                    post.getUser().getId(),
                    userId,
                    NotificationType.LIKE_POST,
                    postId.toString(),
                    TargetType.POST
            );
        }
        return mapToResponse(post);
    }

    // lay 1 reaction cua 1 user
    public ReactionType getUserReactionOnPost(Long postId, Long userId) {
        return reactionRepository.findByTargetIdAndTargetTypeAndUserId(postId, ReactionTargetType.POST, userId)
                .map(Reaction::getType)
                .orElse(null);
    }

    //report

    public void reportPost(Long postId, Long reporterId, ReportRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_EXISTED));

        if (post.getUser().getId().equals(reporterId)) {
            throw new AppException(ErrorCode.CANNOT_REPORT_OWN_CONTENT);
        }

        if (reportRepository.existsByPostAndReporterId(post, reporterId)) {
            throw new AppException(ErrorCode.ALREADY_REPORTED);
        }

        Report report = Report.builder()
                .post(post)
                .reporterId(reporterId)
                .reason(request.getReason())
                .description(request.getDescription())
                .status(ReportStatus.PENDING)
                .build();

        reportRepository.save(report);

        // Tạm ẩn post nếu chưa ẩn
        if (!post.isHidden()) {
            post.setHidden(true);
            postRepository.save(post);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ReportResponse handleReport(Long reportId, ReportStatus newStatus) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new AppException(ErrorCode.REPORT_NOT_FOUND));

        if (report.getStatus() != ReportStatus.PENDING) {
            throw new AppException(ErrorCode.REPORT_ALREADY_HANDLED);
        }

        report.setStatus(newStatus);
        report.setHandledAt(LocalDateTime.now());

        reportRepository.save(report);

        Post post = report.getPost();
        User user=userRepository.findById(post.getUser().getId())
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_EXISTED));
        if (newStatus == ReportStatus.APPROVED) {
            deletePost(post.getUser().getId(), post.getId());
            mailService.sendEmail(
                    user.getEmail(),
                    "Cảnh cáo vi phạm",
                    "bài đăng có nội dụng "+post.getContent()+"đã vi phạm tiêu chuẩn cộng " +
                            "đồng và bị tố cáo chúng tôi đã xem xét và gỡ bỏ bào đăng của bạn"
            );
        } else if (newStatus == ReportStatus.REJECTED) {
            // Hiện lại post
            post.setHidden(false);
            postRepository.save(post);
        }

        return mapToReportResponse(report);
    }

    private ReportResponse mapToReportResponse(Report report) {
        return ReportResponse.builder()
                .id(report.getId())
                .postId(report.getPost().getId())
                .reporterId(report.getReporterId())
                .reason(report.getReason())
                .description(report.getDescription())
                .status(report.getStatus())
                .reportedAt(report.getReportedAt())
                .build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<ReportResponse> getPendingReports() {
        return reportRepository.findByStatusOrderByReportedAtDesc(ReportStatus.PENDING)
                .stream()
                .map(this::mapToReportResponse)
                .collect(Collectors.toList());
    }

    public Slice<PostResponse> searchPosts(String keyword, int page, int size) {
        return postRepository
                .searchPublicPosts(keyword, PageRequest.of(page, size,
                        Sort.by("createdAt").descending()))
                .map(this::mapToResponse);
    }

    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new AppException(ErrorCode.NO_PERMISSION));

        // Chỉ chủ comment hoặc chủ bài viết mới được xóa
        Post post = comment.getPost();
        boolean isOwner = comment.getUserId().equals(userId);
        boolean isPostOwner = post.getUser().getId().equals(userId);

        if (!isOwner && !isPostOwner) {
            throw new AppException(ErrorCode.NO_PERMISSION);
        }

        commentRepository.delete(comment);
    }
    public Slice<PostResponse> getUserPosts(Long targetUserId, Long viewerId,
                                            int page, int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());

        // Xem profile của chính mình — thấy hết
        if (targetUserId.equals(viewerId)) {
            return postRepository
                    .findByUserIdAndIsHiddenFalseAndGroupIsNullOrderByCreatedAtDesc(
                            targetUserId, pageable)
                    .map(this::mapToResponse);
        }
        // Xem người khác — chỉ thấy PUBLIC
        return postRepository
                .findByUserIdAndIsHiddenFalseAndPrivacyAndGroupIsNull(
                        targetUserId, Privacy.PUBLIC, pageable)
                .map(this::mapToResponse);
    }
    public Slice<AdminPostItemResponse> getAdminPosts(int page, int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return postRepository.findAll(pageable)
                .map(this::toAdminPostItemResponse);
    }
    private AdminPostItemResponse toAdminPostItemResponse(Post post) {

        return AdminPostItemResponse.builder()
                .id(post.getId())
                .user(toUserPost(post.getUser()))
                .content(post.getContent())
                .privacy(post.getPrivacy())
                .createdAt(post.getCreatedAt())
                .build();
    }
}