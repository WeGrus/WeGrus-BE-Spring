package wegrus.clubwebsite.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import wegrus.clubwebsite.dto.Status;
import wegrus.clubwebsite.dto.StatusResponse;
import wegrus.clubwebsite.dto.VerificationResponse;
import wegrus.clubwebsite.dto.member.*;
import wegrus.clubwebsite.dto.post.BookmarkDto;
import wegrus.clubwebsite.dto.post.PostDto;
import wegrus.clubwebsite.dto.post.PostReplyDto;
import wegrus.clubwebsite.dto.result.ResultResponse;
import wegrus.clubwebsite.entity.group.Group;
import wegrus.clubwebsite.exception.MemberAlreadyBanException;
import wegrus.clubwebsite.exception.MemberAlreadyResignException;
import wegrus.clubwebsite.exception.MemberNotFoundException;
import wegrus.clubwebsite.service.MemberService;
import wegrus.clubwebsite.util.RedisUtil;

import javax.mail.MessagingException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import java.io.IOException;
import java.util.List;

import static wegrus.clubwebsite.dto.result.ResultCode.*;

@Api(tags = "회원 API")
@Validated
@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final RedisUtil redisUtil;

    @ApiOperation(value = "본인 이메일 인증", notes = "인증된 이메일은 30분간 유효하며, 만료 시 다시 이메일 인증을 받아야 합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "불필요", example = " "),
            @ApiImplicitParam(name = "verificationKey", value = "인증키", required = true, example = "5ecc3d01-bd92-4f1a-bdf2-9a5a777871ae")
    })
    @PostMapping("/signup/verify")
    public ResponseEntity<ResultResponse> verifySchoolEmail(@NotBlank(message = "인증 키는 필수입니다.") @RequestParam String verificationKey) {
        final VerificationResponse response = memberService.checkVerificationKey(verificationKey);

        return ResponseEntity.ok(ResultResponse.of(REQUEST_VERIFY_SUCCESS, response));
    }

    @ApiOperation(value = "회원 가입", notes = "이메일 검증 API에서 받은 토큰과 함께 요청해주세요.")
    @ApiImplicitParam(name = "Authorization", value = "불필요", example = " ")
    @PostMapping("/signup")
    public ResponseEntity<ResultResponse> signup(@RequestBody MemberSignupRequest request) {
        final MemberSignupResponse response = memberService.validateAndSaveMember(request);

        return ResponseEntity.ok(ResultResponse.of(SIGNUP_SUCCESS, response));
    }

    @ApiOperation(value = "로그인")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "불필요", example = " "),
            @ApiImplicitParam(name = "authorizationCode", value = "카카오 인증 코드", required = true, example = "ASKDJASIN12231KNsakdasdl1210SSALadk5234")
    })
    @PostMapping("/signin")
    public ResponseEntity<ResultResponse> signin(@NotBlank(message = "카카오 인증 코드는 필수입니다.") @RequestParam String authorizationCode,
            HttpServletResponse httpServletResponse) {
        try {
            final MemberAndJwtDto dto = memberService.findMemberAndGenerateJwt(authorizationCode);
            final MemberSigninSuccessResponse response = new MemberSigninSuccessResponse(Status.SUCCESS, dto.getMember(), dto.getAccessToken());
            putRefreshTokenToCookie(httpServletResponse, dto.getRefreshToken());

            return ResponseEntity.ok(ResultResponse.of(SIGNIN_SUCCESS, response));
        } catch (MemberNotFoundException e) {
            final String userId = (String) redisUtil.get(authorizationCode);
            final MemberSigninFailResponse response = new MemberSigninFailResponse(Status.FAILURE, userId);
            redisUtil.delete(authorizationCode);

            return ResponseEntity.ok(ResultResponse.of(NEED_TO_SIGNUP, response));
        } catch (MemberAlreadyResignException e) {
            final String userId = (String) redisUtil.get(authorizationCode);
            final MemberSigninFailResponse response = new MemberSigninFailResponse(Status.FAILURE, userId);
            redisUtil.delete(authorizationCode);

            return ResponseEntity.ok(ResultResponse.of(NEED_TO_REJOIN, response));
        } catch (MemberAlreadyBanException e) {
            final String userId = (String) redisUtil.get(authorizationCode);
            final MemberSigninFailResponse response = new MemberSigninFailResponse(Status.FAILURE, userId);
            redisUtil.delete(authorizationCode);

            return ResponseEntity.ok(ResultResponse.of(BANNED_USER, response));
        }
    }

    private void putRefreshTokenToCookie(HttpServletResponse httpServletResponse, String refreshToken) {
        // TODO: HTTPS 적용 후에 secure(true) 적용하기
        //  Frontend 호스팅 시 sameSite 적용하기
        ResponseCookie cookie = ResponseCookie.from("igrus-rt", refreshToken)
                .httpOnly(true)
                .sameSite("strict")
                .domain("igrus.net")
                .maxAge(14 * 24 * 60 * 60)
                .path("/")
                .build();

        httpServletResponse.addHeader("Set-Cookie", cookie.toString());
    }

    @ApiOperation(value = "로그아웃")
    @PostMapping("/signout")
    public ResponseEntity<ResultResponse> signout(HttpServletResponse httpServletResponse) {
        removeCookie(httpServletResponse, "igrus-rt");
        final StatusResponse response = new StatusResponse(Status.SUCCESS);

        return ResponseEntity.ok(ResultResponse.of(SIGNOUT_SUCCESS, response));
    }

    private void removeCookie(HttpServletResponse httpServletResponse, String name) {
        final Cookie cookie = new Cookie(name, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");

        httpServletResponse.addCookie(cookie);
    }

    @ApiOperation(value = "토큰 재발급")
    @ApiImplicitParam(name = "Authorization", value = "불필요", example = " ")
    @PostMapping("/reissue")
    public ResponseEntity<ResultResponse> reissue(
            @NotNull(message = "refreshToken 쿠키는 필수입니다.") @CookieValue(value = "igrus-rt", required = false) Cookie cookie,
            HttpServletResponse httpServletResponse) {
        final JwtDto jwtDto = memberService.reIssueJwt(cookie.getValue());
        putRefreshTokenToCookie(httpServletResponse, jwtDto.getRefreshToken());
        final ReIssueResponse response = new ReIssueResponse(jwtDto.getAccessToken());

        return ResponseEntity.ok(ResultResponse.of(REISSUE_SUCCESS, response));
    }

    @ApiOperation(value = "이메일 검증", notes = "이메일 중복 체크와 검증에 성공하면, 해당 이메일로 인증메일을 전송합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "불필요", example = " "),
            @ApiImplicitParam(name = "email", value = "이메일", required = true, example = "12161542@inha.edu")
    })
    @PostMapping("/signup/check/email")
    public ResponseEntity<ResultResponse> checkEmail(@NotBlank(message = "이메일은 필수입니다.") @RequestParam String email) throws MessagingException {
        final EmailCheckResponse response = memberService.checkEmailAndSendMail(email);

        return ResponseEntity.ok(ResultResponse.of(CHECK_EMAIL_SUCCESS, response));
    }

    @ApiOperation(value = "회원 정보 조회")
    @ApiImplicitParam(name = "memberId", value = "회원 순번(PK)", required = true, example = "1")
    @GetMapping("/members/info/{memberId}")
    public ResponseEntity<ResultResponse> getInfo(@NotNull(message = "회원 순번은 필수입니다.") @PathVariable Long memberId) {
        final MemberInfoResponse response = memberService.getMemberInfo(memberId);

        return ResponseEntity.ok(ResultResponse.of(GET_MEMBER_INFO_SUCCESS, response));
    }

    @ApiOperation(value = "본인 정보 조회")
    @GetMapping("/info")
    public ResponseEntity<ResultResponse> getInfo() {
        final MemberInfoResponse response = memberService.getMemberInfo(Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName()));

        return ResponseEntity.ok(ResultResponse.of(GET_MEMBER_INFO_SUCCESS, response));
    }

    @ApiOperation(value = "회원 정보 수정")
    @PatchMapping("/members/info")
    public ResponseEntity<ResultResponse> updateInfo(@RequestBody MemberInfoUpdateRequest request) {
        final MemberInfoUpdateResponse response = memberService.updateMemberInfo(request);

        return ResponseEntity.ok(ResultResponse.of(UPDATE_MEMBER_INFO_SUCCESS, response));
    }

    @ApiOperation(value = "회원 이미지 변경")
    @ApiImplicitParam(name = "image", value = "회원 이미지")
    @PatchMapping(value = "/members/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResultResponse> updateImage(@RequestPart(required = false, name = "image") MultipartFile image) throws IOException {
        final MemberImageUpdateResponse response = memberService.updateMemberImage(image);

        return ResponseEntity.ok(ResultResponse.of(UPDATE_MEMBER_IMAGE_SUCCESS, response));
    }

    @ApiOperation(value = "이메일 인증 여부 확인")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "불필요", example = " "),
            @ApiImplicitParam(name = "email", value = "이메일", required = true, example = "12161542@inha.edu")
    })
    @GetMapping("/signup/validate/email")
    public ResponseEntity<ResultResponse> validateEmail(@NotBlank(message = "이메일은 필수입니다.") @RequestParam String email) {
        final ValidateEmailResponse response = memberService.validateEmail(email);

        return ResponseEntity.ok(ResultResponse.of(VALIDATE_EMAIL_SUCCESS, response));
    }

    @ApiOperation(value = "동아리 가입 신청")
    @PostMapping("/club/apply")
    public ResponseEntity<ResultResponse> requestAuthority() {
        final RequestAuthorityResponse response = memberService.applyToClub();

        return ResponseEntity.ok(ResultResponse.of(REQUEST_AUTHORITY_SUCCESS, response));
    }

    @ApiOperation(value = "회원 탈퇴")
    @ApiImplicitParam(name = "certificationCode", value = "인증 코드", example = "123456", required = true)
    @PostMapping("/members/resign")
    public ResponseEntity<ResultResponse> resign(@Pattern(regexp = "^[0-9]{6}$", message = "6자리 인증 코드를 입력해주세요.") @RequestParam String certificationCode) {
        final StatusResponse response = memberService.resign(certificationCode);

        return ResponseEntity.ok(ResultResponse.of(MEMBER_RESIGN_SUCCESS, response));
    }

    @ApiOperation(value = "이메일 인증 코드 발송", notes = "30분간 유효한 임의의 6자리 난수를 회원의 이메일로 발송합니다.")
    @PostMapping("/members/verify")
    public ResponseEntity<ResultResponse> sendCertificationCode() {
        final StatusResponse response = memberService.sendRandomCode();

        return ResponseEntity.ok(ResultResponse.of(SEND_CERTIFICATION_CODE_SUCCESS, response));
    }

    @ApiOperation(value = "회원이 작성한 게시물 목록 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "memberId", value = "회원 PK", example = "1", required = true),
            @ApiImplicitParam(name = "page", value = "페이지", example = "1", required = true),
            @ApiImplicitParam(name = "size", value = "페이지당 개수", example = "10", required = true)
    })
    @GetMapping("/members/posts")
    public ResponseEntity<ResultResponse> getMyPosts(
            @NotNull(message = "회원 PK는 필수입니다.") @RequestParam Long memberId,
            @NotNull(message = "게시물 page는 필수입니다.") @RequestParam int page,
            @NotNull(message = "게시물 page당 size는 필수입니다.") @RequestParam int size) {
        final Page<PostDto> response = memberService.getMyPosts(memberId, page, size);

        return ResponseEntity.ok(ResultResponse.of(GET_MY_POSTS_SUCCESS, response));
    }

    @ApiOperation(value = "회원이 작성한 댓글 목록 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "memberId", value = "회원 PK", example = "1", required = true),
            @ApiImplicitParam(name = "page", value = "페이지", example = "1", required = true),
            @ApiImplicitParam(name = "size", value = "페이지당 개수", example = "10", required = true)
    })
    @GetMapping("/members/replies")
    public ResponseEntity<ResultResponse> getMyReplies(
            @NotNull(message = "회원 PK는 필수입니다.") @RequestParam Long memberId,
            @NotNull(message = "게시물 page는 필수입니다.") @RequestParam int page,
            @NotNull(message = "게시물 page당 size는 필수입니다.") @RequestParam int size) {
        final Page<PostReplyDto> response = memberService.getMyReplies(memberId, page, size);

        return ResponseEntity.ok(ResultResponse.of(GET_MY_REPLIES_SUCCESS, response));
    }

    @ApiOperation(value = "회원이 저장한 게시물 목록 조회")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "memberId", value = "회원 PK", example = "1", required = true),
            @ApiImplicitParam(name = "page", value = "페이지", example = "1", required = true),
            @ApiImplicitParam(name = "size", value = "페이지당 개수", example = "10", required = true)
    })
    @GetMapping("/members/bookmarks")
    public ResponseEntity<ResultResponse> getMyBookmarks(
            @NotNull(message = "회원 PK는 필수입니다.") @RequestParam Long memberId,
            @NotNull(message = "게시물 page는 필수입니다.") @RequestParam int page,
            @NotNull(message = "게시물 page당 size는 필수입니다.") @RequestParam int size) {
        final Page<BookmarkDto> response = memberService.getMyBookmarks(memberId, page, size);

        return ResponseEntity.ok(ResultResponse.of(GET_MY_BOOKMARKS_SUCCESS, response));
    }

    @ApiOperation(value = "그룹 가입 신청")
    @ApiImplicitParam(name = "groupId", value = "그룹 PK", example = "1", required = true)
    @PostMapping("/members/groups/apply")
    public ResponseEntity<ResultResponse> applyToGroup(
            @NotNull(message = "그룹 PK는 필수입니다.") @RequestParam Long groupId) {
        final StatusResponse response = memberService.applyToGroup(groupId);

        return ResponseEntity.ok(ResultResponse.of(APPLY_TO_GROUP_SUCCESS, response));
    }

    @ApiOperation(value = "그룹 목록 조회")
    @GetMapping("/members/groups")
    public ResponseEntity<ResultResponse> getGroups() {
        final List<Group> response = memberService.getGroups();

        return ResponseEntity.ok(ResultResponse.of(GET_GROUPS_SUCCESS, response));
    }
}
