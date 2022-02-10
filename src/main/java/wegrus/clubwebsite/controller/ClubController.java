package wegrus.clubwebsite.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import wegrus.clubwebsite.dto.StatusResponse;
import wegrus.clubwebsite.dto.member.RequestDto;
import wegrus.clubwebsite.dto.result.ResultResponse;
import wegrus.clubwebsite.entity.member.MemberRoles;
import wegrus.clubwebsite.service.ClubService;

import javax.validation.constraints.NotNull;

import static wegrus.clubwebsite.dto.result.ResultCode.*;

@Api(tags = "동아리 관리 API")
@Validated
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

    @ApiOperation(value = "동아리원 권한 요청 목록 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "페이지", example = "1", required = true),
            @ApiImplicitParam(name = "size", value = "페이지당 개수", example = "10", required = true),
            @ApiImplicitParam(name = "role", value = "권한", example = "ROLE_MEMBER", required = true)
    })
    @GetMapping("/executives/requests")
    public ResponseEntity<ResultResponse> getRequests(
            @NotNull(message = "page는 필수입니다.") @RequestParam int page,
            @NotNull(message = "size는 필수입니다.") @RequestParam int size,
            @NotNull(message = "권한 타입은 필수입니다.") @RequestParam MemberRoles role) {
        final Page<RequestDto> response = clubService.getRequestDtoPage(page, size, role);

        return ResponseEntity.ok(ResultResponse.of(GET_REQUESTS_SUCCESS, response));
    }
}
