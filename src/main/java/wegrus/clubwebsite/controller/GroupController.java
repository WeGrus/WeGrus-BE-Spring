package wegrus.clubwebsite.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import wegrus.clubwebsite.dto.StatusResponse;
import wegrus.clubwebsite.dto.member.MemberDto;
import wegrus.clubwebsite.dto.member.MemberSortType;
import wegrus.clubwebsite.dto.result.ResultCode;
import wegrus.clubwebsite.dto.result.ResultResponse;
import wegrus.clubwebsite.service.GroupService;

import javax.validation.constraints.NotNull;

import static wegrus.clubwebsite.dto.result.ResultCode.*;

@Api(tags = "그룹 관리 API")
@Validated
@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @ApiOperation(value = "그룹 가입 신청 목록 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "페이지", example = "1", required = true),
            @ApiImplicitParam(name = "size", value = "페이지당 개수", example = "10", required = true),
            @ApiImplicitParam(name = "sortType", value = "정렬 타입", example = "ID", required = true),
            @ApiImplicitParam(name = "direction", value = "정렬 방향", example = "ASC", required = true),
            @ApiImplicitParam(name = "groupId", value = "그룹 PK", example = "1", required = true)
    })
    @GetMapping("/executives/applicants")
    public ResponseEntity<ResultResponse> getApplicants(
            @NotNull(message = "page는 필수입니다.") @RequestParam int page,
            @NotNull(message = "size는 필수입니다.") @RequestParam int size,
            @NotNull(message = "정렬 타입은 필수입니다.") @RequestParam MemberSortType sortType,
            @NotNull(message = "정렬 방향은 필수입니다.") @RequestParam Sort.Direction direction,
            @NotNull(message = "그룹 PK는 필수입니다.") @RequestParam Long groupId) {
        final Page<MemberDto> response = groupService.getApplicants(groupId, page, size, sortType, direction);

        return ResponseEntity.ok(ResultResponse.of(GET_GROUP_APPLICANTS_SUCCESS, response));
    }

    @ApiOperation(value = "그룹 가입 승인")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupId", value = "그룹 PK", example = "1", required = true),
            @ApiImplicitParam(name = "memberId", value = "회원 PK", example = "1", required = true)
    })
    @PatchMapping("/executives/applicants/approve")
    public ResponseEntity<ResultResponse> approve(
            @NotNull(message = "그룹 PK는 필수입니다.") @RequestParam Long groupId,
            @NotNull(message = "회원 PK는 필수입니다.") Long memberId) {
        final StatusResponse response = groupService.approve(groupId, memberId);

        return ResponseEntity.ok(ResultResponse.of(APPROVE_APPLICANT_SUCCESS, response));
    }

    @ApiOperation(value = "그룹 가입 거절")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupId", value = "그룹 PK", example = "1", required = true),
            @ApiImplicitParam(name = "memberId", value = "회원 PK", example = "1", required = true)
    })
    @DeleteMapping("/executives/applicants/reject")
    public ResponseEntity<ResultResponse> reject(
            @NotNull(message = "그룹 PK는 필수입니다.") @RequestParam Long groupId,
            @NotNull(message = "회원 PK는 필수입니다.") Long memberId) {
        final StatusResponse response = groupService.reject(groupId, memberId);

        return ResponseEntity.ok(ResultResponse.of(REJECT_APPLICANT_SUCCESS, response));
    }
}
