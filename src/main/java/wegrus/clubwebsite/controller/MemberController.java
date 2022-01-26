package wegrus.clubwebsite.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import wegrus.clubwebsite.dto.Status;
import wegrus.clubwebsite.dto.StatusResponse;
import wegrus.clubwebsite.dto.VerificationResponse;
import wegrus.clubwebsite.dto.member.*;
import wegrus.clubwebsite.dto.result.ResultResponse;
import wegrus.clubwebsite.entity.member.MemberRoles;
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

import java.io.IOException;

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
    public ResponseEntity<ResultResponse> signup(@Validated @RequestBody MemberSignupRequest request) {
        final MemberSignupResponse response = memberService.validateAndSaveMember(request);

        return ResponseEntity.ok(ResultResponse.of(SIGNUP_SUCCESS, response));
    }

    @ApiOperation(value = "로그인")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "불필요", example = " "),
            @ApiImplicitParam(name = "authorizationCode", value = "카카오 인증 코드", required = true, example = "ASKDJASIN12231KNsakdasdl1210SSALadk5234")
    })
    @PostMapping("/signin")
    public ResponseEntity<ResultResponse> signin(
            @Validated @NotBlank(message = "카카오 인증 코드는 필수입니다.") @RequestParam String authorizationCode,
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
        //  Frontend 호스팅 시 sameSite, domain 적용하기
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
//                .sameSite("strict")
//                .domain("domain")
                .maxAge(14 * 24 * 60 * 60)
                .path("/")
                .build();

        httpServletResponse.addHeader("Set-Cookie", cookie.toString());
    }

    @ApiOperation(value = "로그아웃")
    @PostMapping("/members/signout")
    public ResponseEntity<ResultResponse> signout(@CookieValue(value = "refreshToken") Cookie cookie) {
        memberService.deleteRefreshToken(cookie.getValue());
        final MemberSignoutResponse response = new MemberSignoutResponse(Status.SUCCESS);

        return ResponseEntity.ok(ResultResponse.of(SIGNOUT_SUCCESS, response));
    }

    @ApiOperation(value = "토큰 재발급")
    @ApiImplicitParam(name = "Authorization", value = "불필요", example = " ")
    @PostMapping("/reissue")
    public ResponseEntity<ResultResponse> reissue(
            @CookieValue(value = "refreshToken") Cookie cookie,
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
    public ResponseEntity<ResultResponse> checkEmail(@RequestParam String email) throws MessagingException {
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

    @ApiOperation(value = "회원 정보 수정")
    @PatchMapping("/members/info")
    public ResponseEntity<ResultResponse> updateInfo(@Validated @RequestBody MemberInfoUpdateRequest request) {
        final MemberInfoUpdateResponse response = memberService.updateMemberInfo(request);

        return ResponseEntity.ok(ResultResponse.of(UPDATE_MEMBER_INFO_SUCCESS, response));
    }

    @ApiOperation(value = "회원 이미지 변경")
    @ApiImplicitParam(name = "multipartFile", value = "회원 이미지")
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
    public ResponseEntity<ResultResponse> validateEmail(@RequestParam String email) {
        final ValidateEmailResponse response = memberService.validateEmail(email);

        return ResponseEntity.ok(ResultResponse.of(VALIDATE_EMAIL_SUCCESS, response));
    }

    @ApiOperation(value = "회원 권한 요청")
    @PostMapping("/members/authority")
    public ResponseEntity<ResultResponse> requestAuthority(@NotNull(message = "요청할 권한은 필수입니다.") @RequestParam(name = "role") MemberRoles role) {
        final RequestAuthorityResponse response = memberService.requestAuthority(role);

        return ResponseEntity.ok(ResultResponse.of(REQUEST_AUTHORITY_SUCCESS, response));
    }

    @ApiOperation(value = "회원 탈퇴")
    @PostMapping("/members/resign")
    public ResponseEntity<ResultResponse> resign() {
        final StatusResponse response = memberService.resign();

        return ResponseEntity.ok(ResultResponse.of(MEMBER_RESIGN_SUCCESS, response));
    }
}
