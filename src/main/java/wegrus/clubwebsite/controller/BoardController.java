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
import wegrus.clubwebsite.dto.result.ResultResponse;
import wegrus.clubwebsite.service.BoardService;

import static wegrus.clubwebsite.dto.result.ResultCode.*;

@Api(tags = "게시판 API")
@RequiredArgsConstructor
@RestController
public class BoardController {

    private final BoardService boardService;

    @ApiOperation(value = "게시물 등록")
    @PostMapping("/posts")
    public ResponseEntity<ResultResponse> createBoard(@RequestBody BoardCreateRequest request){
        final Long boardId = boardService.create(request);
        final BoardCreateResponse response = new BoardCreateResponse(boardId);

        return ResponseEntity.ok(ResultResponse.of(CREATE_BOARD_SUCCESS, response));
    }
}
