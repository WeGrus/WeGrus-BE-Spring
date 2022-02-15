package wegrus.clubwebsite.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import wegrus.clubwebsite.dto.post.*;
import wegrus.clubwebsite.dto.result.ResultResponse;
import wegrus.clubwebsite.entity.post.PostListType;
import wegrus.clubwebsite.service.PostService;
import wegrus.clubwebsite.service.ReplyService;

import javax.validation.constraints.NotNull;

import java.io.IOException;

import static wegrus.clubwebsite.dto.result.ResultCode.*;

@Api(tags = "게시판 API")
@RequiredArgsConstructor
@RestController
public class PostController {

    private final PostService postService;
    private final ReplyService replyService;

    @ApiOperation(value = "게시물 이미지 서버 등록")
    @PostMapping("/posts/image")
    public ResponseEntity<ResultResponse> createPostImage(
            @RequestPart(name = "image") MultipartFile image) throws IOException {
        final PostImageCreateResponse response = postService.createPostImage(image);

        return ResponseEntity.ok(ResultResponse.of(CREATE_POST_IMAGE_SUCCESS, response));
    }

    @ApiOperation(value = "게시물 등록")
    @PostMapping(value = "/posts")
    public ResponseEntity<ResultResponse> createPost(
            @RequestPart(name = "postCreateRequest") PostCreateRequest request,
            @RequestPart(name = "file", required = false) MultipartFile file) throws IOException{
        final PostCreateResponse response = postService.create(request, file);

        return ResponseEntity.ok(ResultResponse.of(CREATE_POST_SUCCESS, response));
    }

    @ApiOperation(value = "게시물 수정")
    @PutMapping("/posts")
    public ResponseEntity<ResultResponse> updatePost(
            @Validated @RequestBody PostUpdateRequest request) {
        final Long postId = postService.update(request);
        final PostUpdateResponse response = new PostUpdateResponse(postId);

        return ResponseEntity.ok(ResultResponse.of(UPDATE_POST_SUCCESS, response));
    }

    @ApiOperation(value = "게시물 삭제")
    @DeleteMapping("/posts")
    public ResponseEntity<ResultResponse> deletePost(
            @Validated @NotNull(message = "게시물 id는 필수입니다.") @RequestParam Long postId) {
        postService.delete(postId);

        return ResponseEntity.ok(ResultResponse.of(DELETE_POST_SUCCESS, null));
    }

    @ApiOperation(value = "게시물 조회")
    @ApiImplicitParam(name = "postId", value = "게시물 순번(PK)", required = true, example = "1")
    @GetMapping("/posts/{postId}")
    public ResponseEntity<ResultResponse> getPost(@NotNull(message = "게시물 id는 필수입니다.") @PathVariable Long postId) {
        final PostResponse response = postService.getPost(postId);

        return ResponseEntity.ok(ResultResponse.of(VIEW_POST_SUCCESS, response));
    }

    @ApiOperation(value = "게시물 추천")
    @PostMapping("/posts/like")
    public ResponseEntity<ResultResponse> likePost(
            @Validated @NotNull(message = "게시물 id는 필수입니다.") @RequestParam Long postId) {
        final Long postLikeId = postService.like(postId);
        final PostLikeCreateResponse response = new PostLikeCreateResponse(postLikeId);

        return ResponseEntity.ok(ResultResponse.of(CREATE_POST_LIKE_SUCCESS, response));

    }

    @ApiOperation(value = "게시물 추천 해제")
    @DeleteMapping("/posts/like")
    public ResponseEntity<ResultResponse> dislikePost(
            @Validated @NotNull(message = "게시물 id는 필수입니다.") @RequestParam Long postId) {
        postService.dislike(postId);

        return ResponseEntity.ok(ResultResponse.of(DELETE_POST_LIKE_SUCCESS, null));
    }

    @ApiOperation(value = "게시물 북마크 등록")
    @PostMapping("/members/bookmarks")
    public ResponseEntity<ResultResponse> createBookmark(
            @Validated @NotNull(message = "게시물 id는 필수입니다.") @RequestParam Long postId) {
        final Long bookmarkId = postService.createBookmark(postId);
        final BookmarkCreateResponse response = new BookmarkCreateResponse(bookmarkId);

        return ResponseEntity.ok(ResultResponse.of(CREATE_BOOKMARK_SUCCESS, response));
    }

    @ApiOperation(value = "게시물 북마크 해제")
    @DeleteMapping("/members/bookmarks")
    public ResponseEntity<ResultResponse> deleteBookmark(
            @Validated @NotNull(message = "게시물 id는 필수입니다.") @RequestParam Long postId) {
        postService.deleteBookmark(postId);

        return ResponseEntity.ok(ResultResponse.of(DELETE_BOOKMARK_SUCCESS, null));
    }

    @ApiOperation(value = "댓글 등록")
    @PostMapping("/comments")
    public ResponseEntity<ResultResponse> createComment(
            @Validated @RequestBody ReplyCreateRequest request) {
        final Long replyId = replyService.create(request);
        final ReplyCreateResponse response = new ReplyCreateResponse(replyId);

        return ResponseEntity.ok(ResultResponse.of(CREATE_REPLY_SUCCESS, response));
    }

    @ApiOperation(value = "댓글 삭제")
    @DeleteMapping("/comments")
    public ResponseEntity<ResultResponse> deleteComment(
            @Validated @NotNull(message = "댓글 id는 필수입니다.") @RequestParam Long replyId) {
        replyService.delete(replyId);

        return ResponseEntity.ok(ResultResponse.of(DELETE_REPLY_SUCCESS, null));
    }

    @ApiOperation(value = "댓글 추천")
    @PostMapping("/comments/like")
    public ResponseEntity<ResultResponse> likeComment(
            @Validated @NotNull(message = "댓글 id는 필수입니다.") @RequestParam Long replyId) {
        final Long replyLikeId = replyService.like(replyId);
        final ReplyLikeCreateResponse response = new ReplyLikeCreateResponse(replyLikeId);

        return ResponseEntity.ok(ResultResponse.of(CREATE_REPLY_LIKE_SUCCESS, response));
    }

    @ApiOperation(value = "댓글 추천 해제")
    @DeleteMapping("/comments/like")
    public ResponseEntity<ResultResponse> dislikeComment(
            @Validated @NotNull(message = "댓글 id는 필수입니다.") @RequestParam Long replyId) {
        replyService.dislike(replyId);

        return ResponseEntity.ok(ResultResponse.of(DELETE_REPLY_LIKE_SUCCESS, null));
    }

    @ApiOperation(value = "게시판 조회")
    @GetMapping("boards/categories")
    public ResponseEntity<ResultResponse> getBoards() {
        final BoardResponse response = postService.getBoards();

        return ResponseEntity.ok(ResultResponse.of(VIEW_BOARD_SUCCESS, response));
    }

    @ApiOperation(value = "게시판 추가")
    @PostMapping("club/executives/boards")
    public ResponseEntity<ResultResponse> createBoard(@Validated @RequestBody BoardCreateRequest request) {
        final Long boardId = postService.createBoard(request);
        final BoardCreateResponse response = new BoardCreateResponse(boardId);

        return ResponseEntity.ok(ResultResponse.of(CREATE_BOARD_SUCCESS, response));
    }

    @ApiOperation(value = "게시판 삭제")
    @DeleteMapping("club/executives/boards/{boardId}")
    public ResponseEntity<ResultResponse> deleteBoard(@NotNull(message = "게시판 id는 필수입니다.") @PathVariable Long boardId) {
        postService.deleteBoard(boardId);

        return ResponseEntity.ok(ResultResponse.of(DELETE_BOARD_SUCCESS, null));
    }

    @ApiOperation(value = "게시물 공지여부 변경")
    @PatchMapping("club/executives/boards/pin")
    public ResponseEntity<ResultResponse> updateNoticeFlag(@Validated @RequestBody PostUpdateNoticeRequest request) {
        final PostUpdateNoticeResponse response = postService.updateNotice(request);

        return ResponseEntity.ok(ResultResponse.of(UPDATE_POST_NOTICE_SUCCESS, response));
    }

    @ApiOperation(value = "그룹 공지여부 변경")
    @PatchMapping("groups/executives/boards/pin")
    public ResponseEntity<ResultResponse> groupUpdateNoticeFlag(@Validated @RequestBody PostUpdateNoticeRequest request) {
        final PostUpdateNoticeResponse response = postService.groupUpdateNotice(request);

        return ResponseEntity.ok(ResultResponse.of(UPDATE_GROUP_POST_NOTICE_SUCCESS, response));
    }

    @ApiOperation(value = "게시물 목록 조회")
    @GetMapping("boards/{boardId}")
    public ResponseEntity<ResultResponse> getPostList(
            @NotNull(message = "게시판 id는 필수입니다.") @PathVariable Long boardId,
            @Validated @NotNull(message = "페이지 번호는 필수입니다.") @RequestParam Integer page,
            @Validated @NotNull(message = "페이지 크기는 필수입니다.") @RequestParam Integer pageSize,
            @Validated @NotNull(message = "타입은 필수입니다.") @RequestParam PostListType type) {
        final PostListResponse response = postService.getList(page, pageSize, boardId, type);

        return ResponseEntity.ok(ResultResponse.of(VIEW_POST_LIST_SUCCESS, response));
    }

    @ApiOperation(value = "작성자 검색")
    @GetMapping("search/writer/{boardId}")
    public ResponseEntity<ResultResponse> searchByWriter(
            @NotNull(message = "게시판 id는 필수입니다.") @PathVariable Long boardId,
            @Validated @NotNull(message = "페이지 번호는 필수입니다.") @RequestParam Integer page,
            @Validated @NotNull(message = "페이지 크기는 필수입니다.") @RequestParam Integer pageSize,
            @Validated @NotNull(message = "타입은 필수입니다.") @RequestParam PostListType type,
            @Validated @NotNull(message = "검색어는 필수입니다.") @RequestParam String keyword) {
        final PostListResponse response = postService.searchByWriter(page, pageSize, boardId, type, keyword);

        return ResponseEntity.ok(ResultResponse.of(SEARCH_BY_WRITER_SUCCESS, response));
    }

    @ApiOperation(value = "제목 검색")
    @GetMapping("search/title/{boardId}")
    public ResponseEntity<ResultResponse> searchByTitle(
            @NotNull(message = "게시판 id는 필수입니다.") @PathVariable Long boardId,
            @Validated @NotNull(message = "페이지 번호는 필수입니다.") @RequestParam Integer page,
            @Validated @NotNull(message = "페이지 크기는 필수입니다.") @RequestParam Integer pageSize,
            @Validated @NotNull(message = "타입은 필수입니다.") @RequestParam PostListType type,
            @Validated @NotNull(message = "검색어는 필수입니다.") @RequestParam String keyword) {
        final PostListResponse response = postService.searchByTitle(page, pageSize, boardId, type, keyword);

        return ResponseEntity.ok(ResultResponse.of(SEARCH_BY_TITLE_SUCCESS, response));
    }

    @ApiOperation(value = "제목+내용 검색")
    @GetMapping("search/all/{boardId}")
    public ResponseEntity<ResultResponse> searchByAll(
            @NotNull(message = "게시판 id는 필수입니다.") @PathVariable Long boardId,
            @Validated @NotNull(message = "페이지 번호는 필수입니다.") @RequestParam Integer page,
            @Validated @NotNull(message = "페이지 크기는 필수입니다.") @RequestParam Integer pageSize,
            @Validated @NotNull(message = "타입은 필수입니다.") @RequestParam PostListType type,
            @Validated @NotNull(message = "검색어는 필수입니다.") @RequestParam String keyword) {
        final PostListResponse response = postService.searchByAll(page, pageSize, boardId, type, keyword);

        return ResponseEntity.ok(ResultResponse.of(SEARCH_BY_ALL_SUCCESS, response));
    }
}
