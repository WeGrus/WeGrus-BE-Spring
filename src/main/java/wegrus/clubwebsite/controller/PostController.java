package wegrus.clubwebsite.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import wegrus.clubwebsite.dto.post.*;
import wegrus.clubwebsite.dto.result.ResultResponse;
import wegrus.clubwebsite.entity.post.PostListType;
import wegrus.clubwebsite.service.PostService;
import wegrus.clubwebsite.service.ReplyService;

import javax.validation.constraints.NotNull;

import static wegrus.clubwebsite.dto.result.ResultCode.*;

@Api(tags = "게시판 API")
@RequiredArgsConstructor
@RestController
public class PostController {

    private final PostService postService;
    private final ReplyService replyService;

    @ApiOperation(value = "게시물 등록")
    @PostMapping("/posts")
    public ResponseEntity<ResultResponse> createPost(@Validated @RequestBody PostCreateRequest request){
        final Long postId = postService.create(request);
        final PostCreateResponse response = new PostCreateResponse(postId);

        return ResponseEntity.ok(ResultResponse.of(CREATE_POST_SUCCESS, response));
    }

    @ApiOperation(value = "게시물 수정")
    @PutMapping("/posts")
    public ResponseEntity<ResultResponse> updatePost(
            @Validated @RequestBody PostUpdateRequest request){
        final Long postId = postService.update(request);
        final PostUpdateResponse response = new PostUpdateResponse(postId);

        return ResponseEntity.ok(ResultResponse.of(UPDATE_POST_SUCCESS, response));
    }

    @ApiOperation(value = "게시물 삭제")
    @DeleteMapping("/posts")
    public ResponseEntity<ResultResponse> deletePost(
            @Validated @NotNull(message = "게시물 id는 필수입니다.")@RequestParam Long postId){
        postService.delete(postId);

        return ResponseEntity.ok(ResultResponse.of(DELETE_POST_SUCCESS, null));
    }

    @ApiOperation(value = "게시물 조회")
    @ApiImplicitParam(name = "postId", value = "게시물 순번(PK)", required = true, example = "1")
    @GetMapping("/posts/{postId}")
    public ResponseEntity<ResultResponse> viewPost(@NotNull(message = "게시물 id는 필수입니다.") @PathVariable Long postId){
        final PostResponse response = postService.view(postId);

        return ResponseEntity.ok(ResultResponse.of(VIEW_POST_SUCCESS, response));
    }

    @ApiOperation(value = "게시물 추천")
    @PostMapping("/posts/like")
    public ResponseEntity<ResultResponse> likePost(
            @Validated @NotNull(message = "게시물 id는 필수입니다.")@RequestParam Long postId){
        final Long postLikeId = postService.like(postId);
        final PostLikeCreateResponse response = new PostLikeCreateResponse(postLikeId);

        return ResponseEntity.ok(ResultResponse.of(CREATE_POST_LIKE_SUCCESS, response));

    }

    @ApiOperation(value = "게시물 추천 해제")
    @DeleteMapping("/posts/like")
    public ResponseEntity<ResultResponse> dislikePost(
            @Validated @NotNull(message = "게시물 id는 필수입니다.")@RequestParam Long postId){
        postService.dislike(postId);

        return ResponseEntity.ok(ResultResponse.of(DELETE_POST_LIKE_SUCCESS, null));
    }

    @ApiOperation(value = "댓글 등록")
    @PostMapping("/comments")
    public ResponseEntity<ResultResponse> createComment(
            @Validated @RequestBody ReplyCreateRequest request){
        final Long replyId = replyService.create(request);
        final ReplyCreateResponse response = new ReplyCreateResponse(replyId);

        return ResponseEntity.ok(ResultResponse.of(CREATE_REPLY_SUCCESS, response));
    }

    @ApiOperation(value = "댓글 삭제")
    @DeleteMapping("/comments")
    public ResponseEntity<ResultResponse> deleteComment(
            @Validated @NotNull(message = "댓글 id는 필수입니다.")@RequestParam Long replyId){
        replyService.delete(replyId);

        return ResponseEntity.ok(ResultResponse.of(DELETE_REPLY_SUCCESS, null));
    }

    @ApiOperation(value = "댓글 추천")
    @PostMapping("/comments/like")
    public ResponseEntity<ResultResponse> likeComment(
            @Validated @NotNull(message = "댓글 id는 필수입니다.")@RequestParam Long replyId){
        final Long replyLikeId = replyService.like(replyId);
        final ReplyLikeCreateResponse response = new ReplyLikeCreateResponse(replyLikeId);

        return ResponseEntity.ok(ResultResponse.of(CREATE_REPLY_LIKE_SUCCESS, response));
    }

    @ApiOperation(value = "댓글 추천 해제")
    @DeleteMapping("/comments/like")
    public ResponseEntity<ResultResponse> dislikeComment(
            @Validated @NotNull(message = "댓글 id는 필수입니다.")@RequestParam Long replyId){
        replyService.dislike(replyId);

        return ResponseEntity.ok(ResultResponse.of(DELETE_REPLY_LIKE_SUCCESS, null));
    }

    @ApiOperation(value = "게시판 조회 api")
    @GetMapping("club/executives/boards")
    public ResponseEntity<ResultResponse> viewBoard(){
        final BoardResponse response = postService.viewBoard();

        return ResponseEntity.ok(ResultResponse.of(VIEW_BOARD_SUCCESS, response));
    }

    @ApiOperation(value = "게시판 추가 api")
    @PostMapping("club/executives/boards")
    public ResponseEntity<ResultResponse> createBoard(@Validated @RequestBody BoardCreateRequest request) {
        final Long boardId = postService.createBoard(request);
        final BoardCreateResponse response = new BoardCreateResponse(boardId);

        return ResponseEntity.ok(ResultResponse.of(CREATE_BOARD_SUCCESS, response));
    }

    @ApiOperation(value = "게시판 삭제")
    @DeleteMapping("club/executives/boards/{boardId}")
    public ResponseEntity<ResultResponse> deleteBoard(@NotNull(message = "게시판 id는 필수입니다.") @PathVariable Long boardId){
        postService.deleteBoard(boardId);

        return ResponseEntity.ok(ResultResponse.of(DELETE_BOARD_SUCCESS, null));
    }

    @ApiOperation(value = "게시물 목록 조회")
    @GetMapping("boards/{boardId}")
    public ResponseEntity<ResultResponse> viewPostList(
            @NotNull(message = "게시판 id는 필수입니다.") @PathVariable Long boardId,
            @Validated @NotNull(message = "페이지 번호는 필수입니다.")@RequestParam Integer page,
            @Validated @NotNull(message = "페이지 크기는 필수입니다.")@RequestParam Integer pageSize,
            @Validated @NotNull(message = "타입은 필수입니다.")@RequestParam PostListType type){
        final PostListResponse response = postService.viewList(page, pageSize, boardId, type);

        return ResponseEntity.ok(ResultResponse.of(VIEW_POST_LIST_SUCCESS, response));
    }
}
