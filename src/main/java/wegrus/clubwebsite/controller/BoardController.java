package wegrus.clubwebsite.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import wegrus.clubwebsite.dto.board.BoardCreateRequest;
import wegrus.clubwebsite.dto.board.BoardCreateResponse;
import wegrus.clubwebsite.dto.board.ReplyCreateRequest;
import wegrus.clubwebsite.dto.board.ReplyCreateResponse;
import wegrus.clubwebsite.dto.result.ResultResponse;
import wegrus.clubwebsite.service.BoardService;
import wegrus.clubwebsite.service.ReplyService;

import static wegrus.clubwebsite.dto.result.ResultCode.*;

@Api(tags = "게시판 API")
@RequiredArgsConstructor
@RestController
public class BoardController {

    private final BoardService boardService;
    private final ReplyService replyService;

    @ApiOperation(value = "게시물 등록")
    @PostMapping("/posts")
    public ResponseEntity<ResultResponse> createBoard(@RequestBody BoardCreateRequest request){
        final Long boardId = boardService.create(request);
        final BoardCreateResponse response = new BoardCreateResponse(boardId);

        return ResponseEntity.ok(ResultResponse.of(CREATE_BOARD_SUCCESS, response));
    }

    @ApiOperation(value = "댓글 등록")
    @PostMapping("/comments")
    public ResponseEntity<ResultResponse> createComment(@RequestBody ReplyCreateRequest request){
        final Long replyId = replyService.create(request);
        final ReplyCreateResponse response = new ReplyCreateResponse(replyId);

        return ResponseEntity.ok(ResultResponse.of(CREATE_REPLY_SUCCESS, response));
    }
}
