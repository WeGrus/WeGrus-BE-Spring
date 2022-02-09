package wegrus.clubwebsite.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import wegrus.clubwebsite.dto.StatusResponse;
import wegrus.clubwebsite.dto.result.ResultResponse;
import wegrus.clubwebsite.service.ClubService;

import javax.validation.constraints.NotNull;

import static wegrus.clubwebsite.dto.result.ResultCode.*;

@Api(tags = "동아리 관리 API")
@RestController
@RequestMapping("/club")
@RequiredArgsConstructor
public class ClubController {

    private final ClubService clubService;

    @ApiOperation(value = "동아리원 권한 부여")
    @ApiImplicitParam(name = "requestId", value = "권한 요청 PK", example = "1", required = true)
    @PostMapping("/executives/authority")
    public ResponseEntity<ResultResponse> empower(
            @NotNull(message = "권한 요청 PK는 필수입니다.") @RequestParam Long requestId) {
        final StatusResponse response = clubService.empower(requestId);

        return ResponseEntity.ok(ResultResponse.of(EMPOWER_MEMBER_SUCCESS, response));
    }
}
