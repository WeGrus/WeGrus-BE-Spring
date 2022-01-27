package wegrus.clubwebsite.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import wegrus.clubwebsite.dto.post.*;
import wegrus.clubwebsite.dto.result.ResultResponse;
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
}
