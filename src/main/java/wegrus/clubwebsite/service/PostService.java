package wegrus.clubwebsite.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import wegrus.clubwebsite.dto.post.*;
import wegrus.clubwebsite.entity.member.MemberRole;
import wegrus.clubwebsite.entity.member.MemberRoles;
import wegrus.clubwebsite.entity.post.*;
import wegrus.clubwebsite.entity.member.Member;
import wegrus.clubwebsite.exception.*;
import wegrus.clubwebsite.repository.*;
import wegrus.clubwebsite.util.AmazonS3Util;
import wegrus.clubwebsite.vo.File;
import wegrus.clubwebsite.vo.Image;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PostService {
    private final BoardRepository boardRepository;
    private final BoardCategoryRepository boardCategoryRepository;
    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final PostFileRepository postFileRepository;
    private final MemberRepository memberRepository;
    private final MemberRoleRepository memberRoleRepository;
    private final ReplyRepository replyRepository;
    private final PostLikeRepository postLikeRepository;
    private final ReplyLikeRepository replyLikeRepository;
    private final ViewRepository viewRepository;
    private final BookmarkRepository bookmarkRepository;

    private final AmazonS3Util amazonS3Util;

    @Transactional
    public PostImageCreateResponse createPostImage(MultipartFile multipartFile) throws IOException {
        final String dirName = "posts/temp";

        final Image image = amazonS3Util.uploadImage(multipartFile, dirName);

        PostImage postImage = PostImage.builder()
                .image(image)
                .build();

        return new PostImageCreateResponse(image.getUrl(), postImageRepository.save(postImage).getId());
    }

    @Transactional
    public PostCreateResponse create(PostCreateRequest request, MultipartFile multipartFile) throws IOException {
        String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
        final Member member = memberRepository.findById(Long.valueOf(memberId)).orElseThrow(MemberNotFoundException::new);
        final Board board = boardRepository.findById(request.getBoardId()).orElseThrow(BoardNotFoundException::new);
        PostState state = PostState.ACTIVATE; // ???????????? ???????????? ?????? ????????? ???????????? ??????

        Post post = Post.builder()
                .member(member)
                .board(board)
                .type(request.getType())
                .title(request.getTitle())
                .content(request.getContent())
                .secretFlag(request.isSecretFlag())
                .state(state)
                .build();

        final Long postId = postRepository.save(post).getId();


        // ?????? ?????????
        if (multipartFile != null) {
            final String dirName = "files/posts/" + postId;

            amazonS3Util.createDirectory(dirName);

            final File file = amazonS3Util.uploadFile(multipartFile, dirName);

            PostFile postFile = PostFile.builder()
                    .file(file)
                    .post(post)
                    .build();

            postFileRepository.save(postFile);
        }

        // ????????? ?????? ???????????? ??????
        List<Long> postImageIds = request.getPostImageIds();

        if (!postImageIds.isEmpty()) {
            List<PostImage> postImages = postImageIds.stream()
                    .map(m -> postImageRepository.findById(m).orElseThrow(PostImageNotFoundException::new))
                    .collect(Collectors.toList());

            final String prevDirName = "posts/temp";
            final String dirName = "posts/" + postId;

            amazonS3Util.createDirectory(dirName);

            for (PostImage postImage : postImages) {
                postImage.updatePost(post);
                final Image image = amazonS3Util.updateImage(postImage.getImage(), prevDirName, dirName);
            }
        }

        return new PostCreateResponse(postId);
    }

    @Transactional
    public Long update(PostUpdateRequest request) {
        String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
        final Member member = memberRepository.findById(Long.valueOf(memberId)).orElseThrow(MemberNotFoundException::new);

        final Post post = postRepository.findById(request.getPostId()).orElseThrow(PostNotFoundException::new);

        if (!member.getId().equals(post.getMember().getId())) {
            throw new PostMemberNotMatchException();
        }

        post.update(request);
        return post.getId();
    }

    @Transactional
    public void delete(Long postId) {
        String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
        final Member member = memberRepository.findById(Long.valueOf(memberId)).orElseThrow(MemberNotFoundException::new);

        final Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);

        if (!member.getId().equals(post.getMember().getId())) {
            throw new PostMemberNotMatchException();
        }

        // ????????? ?????? ?????? ??????
        replyLikeRepository.deleteReplyLikesByPost(postId);

        // ?????? ??????
        replyRepository.deleteRepliesByPost(post);

        // ????????? ?????? ?????? ??????
        postLikeRepository.deletePostLikesByPost(post);

        // ????????? ??????
        viewRepository.deleteViewsByPost(post);

        // ????????? ??????
        bookmarkRepository.deleteBookmarksByPost(post);

        // ????????? ??????
        List<PostImage> postImages = post.getImages();
        for (PostImage postImage : postImages) {
            amazonS3Util.deleteImage(postImage.getImage(), "posts/" + postId);
        }
        postImageRepository.deletePostImagesByPost(post);

        // ?????? ??????
        List<PostFile> postFiles = post.getFiles();
        for (PostFile postFile : postFiles) {
            amazonS3Util.deleteFile(postFile.getFile(), "files/posts/" + postId);
        }
        postFileRepository.deletePostFilesByPost(post);

        postRepository.delete(post);
    }

    @Transactional
    public PostResponse getPost(Long postId) {
        String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
        final Member member = memberRepository.findById(Long.valueOf(memberId)).orElseThrow(MemberNotFoundException::new);

        final Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);

        // ????????? ??????
        Optional<View> views = viewRepository.findByMemberAndPost(member, post);

        if (views.isEmpty()) {
            View view = View.builder()
                    .member(member)
                    .post(post)
                    .build();
            viewRepository.save(view);
        }

        // ?????? ??????
        List<ReplyDto> replies = post.getReplies()
                .stream()
                .map(m -> new ReplyDto(m, Long.valueOf(memberId)))
                .collect(Collectors.toList());

        // ?????? ?????? ??????, ????????? ??????
        boolean userPostLiked = false;
        boolean userPostBookmarked = false;

        Optional<PostLike> postLikes = postLikeRepository.findByMemberAndPost(member, post);
        if (postLikes.isPresent())
            userPostLiked = true;

        Optional<Bookmark> bookmarks = bookmarkRepository.findByMemberAndPost(member, post);
        if (bookmarks.isPresent())
            userPostBookmarked = true;

        // ???????????? ????????? ??????
        List<String> postFileUrls = new ArrayList<>();
        Optional<PostFile> postFiles = postFileRepository.findByPostId(postId);
        if (postFiles.isPresent()) {
            postFileUrls = postFiles.stream()
                    .map(s -> s.getFile().getUrl())
                    .collect(Collectors.toList());
        }

        // ????????? ?????? ?????? ????????? '??? ??? ??????' ??????
        List<MemberRole> memberRoles = memberRoleRepository.findAllByMemberId(post.getMember().getId());
        List<String> roles = memberRoles.stream()
                .map(s -> s.getRole().getName())
                .collect(Collectors.toList());

        if (roles.contains(MemberRoles.ROLE_BAN.name()) || roles.contains(MemberRoles.ROLE_RESIGN.name()))
            return new PostResponse(new PostUnknownDto(post, userPostLiked, userPostBookmarked, postFileUrls), replies);

        return new PostResponse(new PostDto(post, userPostLiked, userPostBookmarked, postFileUrls), replies);
    }

    @Transactional
    public Long like(Long postId) {
        String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
        final Member member = memberRepository.findById(Long.valueOf(memberId)).orElseThrow(MemberNotFoundException::new);

        final Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);

        Optional<PostLike> postLikes = postLikeRepository.findByMemberAndPost(member, post);

        // ?????? ????????? ?????????
        if (postLikes.isPresent()) {
            throw new PostLikeAlreadyExistException();
        }

        PostLike postLike = PostLike.builder()
                .post(post)
                .member(member)
                .build();

        postLikeRepository.save(postLike);

        post.likeNum(post.getPostLikeNum() + 1);

        return postLike.getId();
    }

    @Transactional
    public void dislike(Long postId) {
        String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
        final Member member = memberRepository.findById(Long.valueOf(memberId)).orElseThrow(MemberNotFoundException::new);

        final Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);

        final PostLike postLike = postLikeRepository.findByMemberAndPost(member, post).orElseThrow(PostLikeNotFoundException::new);

        postLikeRepository.delete(postLike);

        post.likeNum(post.getPostLikeNum() - 1);
    }

    @Transactional
    public Long createBookmark(Long postId) {
        String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
        final Member member = memberRepository.findById(Long.valueOf(memberId)).orElseThrow(MemberNotFoundException::new);

        final Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);

        Optional<Bookmark> bookmarks = bookmarkRepository.findByMemberAndPost(member, post);

        if (bookmarks.isPresent())
            throw new BookmarkAlreadyExistException();

        Bookmark bookmark = Bookmark.builder()
                .post(post)
                .member(member)
                .build();

        bookmarkRepository.save(bookmark);

        return bookmark.getId();
    }

    @Transactional
    public void deleteBookmark(Long postId) {
        String memberId = SecurityContextHolder.getContext().getAuthentication().getName();
        final Member member = memberRepository.findById(Long.valueOf(memberId)).orElseThrow(MemberNotFoundException::new);

        final Post post = postRepository.findById(postId).orElseThrow(PostNotFoundException::new);

        final Bookmark bookmark = bookmarkRepository.findByMemberAndPost(member, post).orElseThrow(BookmarkNotFoundException::new);

        bookmarkRepository.delete(bookmark);
    }

    @Transactional(readOnly = true)
    public BoardResponse getBoards() {
        List<BoardDto> boardDtos = boardRepository.findAll()
                .stream()
                .map(BoardDto::new)
                .collect(Collectors.toList());

        return new BoardResponse(boardDtos);
    }

    @Transactional
    public Long createBoard(BoardCreateRequest request) {
        final BoardCategory boardCategory = boardCategoryRepository.findById(request.getBoardCategoryId()).orElseThrow(BoardCategoryNotFoundException::new);

        Board board = Board.builder()
                .name(request.getBoardName())
                .boardCategory(boardCategory)
                .build();

        return boardRepository.save(board).getId();
    }

    @Transactional
    public void deleteBoard(Long boardId) {
        boardRepository.deleteById(boardId);
    }

    @Transactional
    public PostUpdateNoticeResponse updateNotice(PostUpdateNoticeRequest request) {
        final Post post = postRepository.findById(request.getPostId()).orElseThrow(PostNotFoundException::new);
        final String postBoardCategoryName = post.getBoard().getBoardCategory().getName();

        // ?????????, ???????????? ?????? ?????????
        if (postBoardCategoryName.equals(BoardCategories.GROUP.name()) || postBoardCategoryName.equals(BoardCategories.STUDY.name()))
            throw new CannotUpdateGroupPostException();

        post.updateNotice(request);
        return new PostUpdateNoticeResponse(request.getPostId(), request.getType());
    }

    @Transactional
    public PostUpdateNoticeResponse groupUpdateNotice(PostUpdateNoticeRequest request) {
        final Post post = postRepository.findById(request.getPostId()).orElseThrow(PostNotFoundException::new);
        final String postBoardCategoryName = post.getBoard().getBoardCategory().getName();

        // ????????????, ??????????????? ?????? ?????????
        if (postBoardCategoryName.equals(BoardCategories.NOTICE.name()) || postBoardCategoryName.equals(BoardCategories.BOARD.name()))
            throw new CannotUpdateNonGroupPostException();

        post.updateNotice(request);
        return new PostUpdateNoticeResponse(request.getPostId(), request.getType());
    }

    @Transactional
    public PostListResponse getList(Integer page, Integer pageSize, Long boardId, PostListType type) {
        Pageable pageable = PageRequest.of(page, pageSize);
        final Board board = boardRepository.findById(boardId).orElseThrow(BoardNotFoundException::new);
        Page<Post> posts;

        if (type == PostListType.LASTEST)
            posts = postRepository.findByBoardOrderByTypeDescIdDesc(board, pageable);
        else if (type == PostListType.LIKEEST)
            posts = postRepository.findByBoardOrderByTypeDescPostLikeNumDescIdDesc(board, pageable);
        else if (type == PostListType.REPLYEST)
            posts = postRepository.findByBoardOrderByTypeDescPostReplyNumDescIdDesc(board, pageable);
        else
            throw new PostListTypeNotFoundException();

        List<PostListDto> postDtos = posts.stream()
                .map(PostListDto::new)
                .collect(Collectors.toList());

        return new PostListResponse(new PageImpl<>(postDtos, pageable, posts.getTotalElements()));
    }

    @Transactional
    public PostListResponse searchByWriter(Integer page, Integer pageSize, Long boardId, PostListType type, String keyword) {
        Pageable pageable = PageRequest.of(page, pageSize);
        final Board board = boardRepository.findById(boardId).orElseThrow(BoardNotFoundException::new);
        Page<Post> posts;

        if (type == PostListType.LASTEST)
            posts = postRepository.findByBoardAndMemberNameContainingIgnoreCaseOrderByTypeDescIdDesc(board, keyword, pageable);
        else if (type == PostListType.LIKEEST)
            posts = postRepository.findByBoardAndMemberNameContainingIgnoreCaseOrderByTypeDescPostLikeNumDescIdDesc(board, keyword, pageable);
        else if (type == PostListType.REPLYEST)
            posts = postRepository.findByBoardAndMemberNameContainingIgnoreCaseOrderByTypeDescPostReplyNumDescIdDesc(board, keyword, pageable);
        else
            throw new PostListTypeNotFoundException();

        List<PostListDto> postDtos = posts.stream()
                .map(PostListDto::new)
                .collect(Collectors.toList());

        return new PostListResponse(new PageImpl<>(postDtos, pageable, posts.getTotalElements()));
    }

    @Transactional
    public PostListResponse searchByTitle(Integer page, Integer pageSize, Long boardId, PostListType type, String keyword) {
        Pageable pageable = PageRequest.of(page, pageSize);
        final Board board = boardRepository.findById(boardId).orElseThrow(BoardNotFoundException::new);
        Page<Post> posts;

        if (type == PostListType.LASTEST)
            posts = postRepository.findByBoardAndTitleContainingIgnoreCaseOrderByTypeDescIdDesc(board, keyword, pageable);
        else if (type == PostListType.LIKEEST)
            posts = postRepository.findByBoardAndTitleContainingIgnoreCaseOrderByTypeDescPostLikeNumDescIdDesc(board, keyword, pageable);
        else if (type == PostListType.REPLYEST)
            posts = postRepository.findByBoardAndTitleContainingIgnoreCaseOrderByTypeDescPostReplyNumDescIdDesc(board, keyword, pageable);
        else
            throw new PostListTypeNotFoundException();

        List<PostListDto> postDtos = posts.stream()
                .map(PostListDto::new)
                .collect(Collectors.toList());

        return new PostListResponse(new PageImpl<>(postDtos, pageable, posts.getTotalElements()));
    }

    @Transactional
    public PostListResponse searchByAll(Integer page, Integer pageSize, Long boardId, PostListType type, String keyword) {
        Pageable pageable = PageRequest.of(page, pageSize);
        final Board board = boardRepository.findById(boardId).orElseThrow(BoardNotFoundException::new);
        Page<Post> posts;

        if (type == PostListType.LASTEST)
            posts = postRepository.findByBoardAndTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrderByTypeDescIdDesc(board, keyword, keyword, pageable);
        else if (type == PostListType.LIKEEST)
            posts = postRepository.findByBoardAndTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrderByTypeDescPostLikeNumDescIdDesc(board, keyword, keyword, pageable);
        else if (type == PostListType.REPLYEST)
            posts = postRepository.findByBoardAndTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrderByTypeDescPostReplyNumDescIdDesc(board, keyword, keyword, pageable);
        else
            throw new PostListTypeNotFoundException();

        List<PostListDto> postDtos = posts.stream()
                .map(PostListDto::new)
                .collect(Collectors.toList());

        return new PostListResponse(new PageImpl<>(postDtos, pageable, posts.getTotalElements()));
    }

}
