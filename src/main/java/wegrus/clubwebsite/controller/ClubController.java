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
import wegrus.clubwebsite.dto.member.MemberSearchType;
import wegrus.clubwebsite.dto.StatusResponse;
import wegrus.clubwebsite.dto.member.MemberDto;
import wegrus.clubwebsite.dto.member.MemberSortType;
import wegrus.clubwebsite.dto.member.RequestDto;
import wegrus.clubwebsite.dto.result.ResultResponse;
import wegrus.clubwebsite.entity.member.MemberRoles;
import wegrus.clubwebsite.service.ClubService;

import javax.validation.constraints.NotBlank;
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

    @ApiOperation(value = "회원 목록 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "페이지", example = "1", required = true),
            @ApiImplicitParam(name = "size", value = "페이지당 개수", example = "10", required = true),
            @ApiImplicitParam(name = "type", value = "정렬 타입", example = "ID", required = true),
            @ApiImplicitParam(name = "direction", value = "정렬 방향", example = "ASC", required = true)
    })
    @GetMapping("/executives/members")
    public ResponseEntity<ResultResponse> getMembers(
            @NotNull(message = "page는 필수입니다.") @RequestParam int page,
            @NotNull(message = "size는 필수입니다.") @RequestParam int size,
            @NotNull(message = "정렬 타입은 필수입니다.") @RequestParam MemberSortType type,
            @NotNull(message = "정렬 방향은 필수입니다.") @RequestParam Sort.Direction direction) {
        final Page<MemberDto> response = clubService.getMemberDtoPage(page, size, type, direction);

        return ResponseEntity.ok(ResultResponse.of(GET_MEMBERS_SUCCESS, response));
    }

    @ApiOperation(value = "회원 검색(검색어)")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "페이지", example = "1", required = true),
            @ApiImplicitParam(name = "size", value = "페이지당 개수", example = "10", required = true),
            @ApiImplicitParam(name = "sortType", value = "정렬 타입", example = "ID", required = true),
            @ApiImplicitParam(name = "direction", value = "정렬 방향", example = "ASC", required = true),
            @ApiImplicitParam(name = "searchType", value = "검색 타입", example = "DEPARTMENT", required = true),
            @ApiImplicitParam(name = "word", value = "검색어", example = "컴퓨터공학과", required = true)
    })
    @GetMapping("/executives/search")
    public ResponseEntity<ResultResponse> searchMembers(
            @NotNull(message = "page는 필수입니다.") @RequestParam int page,
            @NotNull(message = "size는 필수입니다.") @RequestParam int size,
            @NotNull(message = "정렬 타입은 필수입니다.") @RequestParam MemberSortType sortType,
            @NotNull(message = "정렬 방향은 필수입니다.") @RequestParam Sort.Direction direction,
            @NotNull(message = "검색 타입은 필수입니다.") @RequestParam MemberSearchType searchType,
            @NotBlank(message = "검색어는 필수입니다.") @RequestParam String word) {
        final Page<MemberDto> response = clubService.searchMember(page, size, sortType, direction, searchType, word);

        return ResponseEntity.ok(ResultResponse.of(SEARCH_MEMBER_SUCCESS, response));
    }
}
