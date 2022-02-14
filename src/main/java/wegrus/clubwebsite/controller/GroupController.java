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
import wegrus.clubwebsite.dto.result.ResultResponse;
import wegrus.clubwebsite.entity.group.GroupRoles;
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

    @ApiOperation(value = "그룹 가입 승인")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupId", value = "그룹 PK", example = "1", required = true),
            @ApiImplicitParam(name = "memberId", value = "회원 PK", example = "1", required = true)
    })
    @PatchMapping("/executives/applicants/approve")
    public ResponseEntity<ResultResponse> approve(
            @NotNull(message = "그룹 PK는 필수입니다.") @RequestParam Long groupId,
            @NotNull(message = "회원 PK는 필수입니다.") @RequestParam Long memberId) {
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
            @NotNull(message = "회원 PK는 필수입니다.") @RequestParam Long memberId) {
        final StatusResponse response = groupService.reject(groupId, memberId);

        return ResponseEntity.ok(ResultResponse.of(REJECT_APPLICANT_SUCCESS, response));
    }
    
    @ApiOperation(value = "그룹 임원 권한 부여")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupId", value = "그룹 PK", example = "1", required = true),
            @ApiImplicitParam(name = "memberId", value = "회원 PK", example = "1", required = true)
    })
    @PatchMapping("/president/promote")
    public ResponseEntity<ResultResponse> promote(
            @NotNull(message = "그룹 PK는 필수입니다.") @RequestParam Long groupId,
            @NotNull(message = "회원 PK는 필수입니다.") @RequestParam Long memberId) {
        final StatusResponse response = groupService.promote(groupId, memberId);

        return ResponseEntity.ok(ResultResponse.of(PROMOTE_MEMBER_SUCCESS, response));
    }

    @ApiOperation(value = "그룹 임원 권한 해제")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupId", value = "그룹 PK", example = "1", required = true),
            @ApiImplicitParam(name = "memberId", value = "회원 PK", example = "1", required = true)
    })
    @PatchMapping("/president/degrade")
    public ResponseEntity<ResultResponse> degrade(
            @NotNull(message = "그룹 PK는 필수입니다.") @RequestParam Long groupId,
            @NotNull(message = "회원 PK는 필수입니다.") @RequestParam Long memberId) {
        final StatusResponse response = groupService.degrade(groupId, memberId);

        return ResponseEntity.ok(ResultResponse.of(DEGRADE_MEMBER_SUCCESS, response));
    }

    @ApiOperation(value = "그룹 회장 위임")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupId", value = "그룹 PK", example = "1", required = true),
            @ApiImplicitParam(name = "memberId", value = "회원 PK", example = "1", required = true)
    })
    @PatchMapping("/president/delegate")
    public ResponseEntity<ResultResponse> delegate(
            @NotNull(message = "그룹 PK는 필수입니다.") @RequestParam Long groupId,
            @NotNull(message = "회원 PK는 필수입니다.") @RequestParam Long memberId) {
        final StatusResponse response = groupService.delegate(groupId, memberId);

        return ResponseEntity.ok(ResultResponse.of(DEGRADE_MEMBER_SUCCESS, response));
    }

    @ApiOperation(value = "그룹원 강제 탈퇴")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupId", value = "그룹 PK", example = "1", required = true),
            @ApiImplicitParam(name = "memberId", value = "회원 PK", example = "1", required = true)
    })
    @PatchMapping("/president/kick")
    public ResponseEntity<ResultResponse> kickMember(
            @NotNull(message = "그룹 PK는 필수입니다.") @RequestParam Long groupId,
            @NotNull(message = "회원 PK는 필수입니다.") @RequestParam Long memberId) {
        final StatusResponse response = groupService.kickMember(groupId, memberId);

        return ResponseEntity.ok(ResultResponse.of(KICK_GROUP_MEMBER_SUCCESS, response));
    }

    @ApiOperation(value = "그룹원 목록 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "groupId", value = "그룹 PK", example = "1", required = true),
            @ApiImplicitParam(name = "role", value = "그룹 권한", example = "MEMBER", required = true),
            @ApiImplicitParam(name = "page", value = "페이지", example = "1", required = true),
            @ApiImplicitParam(name = "size", value = "페이지당 개수", example = "10", required = true),
            @ApiImplicitParam(name = "type", value = "정렬 타입", example = "ID", required = true),
            @ApiImplicitParam(name = "direction", value = "정렬 방향", example = "ASC", required = true)
    })
    @GetMapping("/executives/members")
    public ResponseEntity<ResultResponse> getMembers(
            @NotNull(message = "그룹 PK는 필수입니다.") @RequestParam Long groupId,
            @NotNull(message = "그룹 PK는 필수입니다.") @RequestParam GroupRoles role,
            @NotNull(message = "page는 필수입니다.") @RequestParam int page,
            @NotNull(message = "size는 필수입니다.") @RequestParam int size,
            @NotNull(message = "정렬 타입은 필수입니다.") @RequestParam MemberSortType type,
            @NotNull(message = "정렬 방향은 필수입니다.") @RequestParam Sort.Direction direction) {
        final Page<MemberDto> response = groupService.getMemberDtoPage(groupId, role, page, size, type, direction);

        return ResponseEntity.ok(ResultResponse.of(GET_GROUP_MEMBERS_SUCCESS, response));
    }
}
