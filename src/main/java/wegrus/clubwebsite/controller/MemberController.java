package wegrus.clubwebsite.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import wegrus.clubwebsite.dto.Status;
import wegrus.clubwebsite.dto.VerificationResponse;
import wegrus.clubwebsite.dto.member.*;
import wegrus.clubwebsite.dto.result.ResultResponse;
import wegrus.clubwebsite.exception.MemberNotFoundException;
import wegrus.clubwebsite.service.MemberService;

import javax.mail.MessagingException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import static wegrus.clubwebsite.dto.result.ResultCode.*;

@Api(tags = "회원 API")
@Validated
@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @ApiOperation(value = "본인 이메일 인증")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "불필요", example = " "),
            @ApiImplicitParam(name = "verificationKey", value = "인증키", required = true, example = "5ecc3d01-bd92-4f1a-bdf2-9a5a777871ae")
    })
    @PostMapping("/signup/verify")
    public ResponseEntity<ResultResponse> verifySchoolEmail(@NotBlank(message = "인증 키는 필수입니다.") @RequestParam String verificationKey) {
        final VerificationResponse response = memberService.checkVerificationKey(verificationKey);

        return ResponseEntity.ok(ResultResponse.of(REQUEST_VERIFY_SUCCESS, response));
    }

    @ApiOperation(value = "회원 가입")
    @ApiImplicitParam(name = "Authorization", value = "불필요", example = " ")
    @PostMapping("/signup")
    public ResponseEntity<ResultResponse> signup(@Validated @RequestBody MemberSignupRequest request) throws MessagingException {
        final MemberSignupResponse response = memberService.validateAndSendVerificationMailAndSaveMember(request);

        return ResponseEntity.ok(ResultResponse.of(SIGNUP_SUCCESS, response));
    }

    @ApiOperation(value = "로그인")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "불필요", example = " "),
            @ApiImplicitParam(name = "kakaoId", value = "카카오 회원 번호", required = true, example = "123456789")
    })
    @PostMapping("/signin")
    public ResponseEntity<ResultResponse> signin(
            @Validated @NotNull(message = "카카오 회원 번호는 필수입니다.") @RequestParam Long kakaoId,
            HttpServletResponse httpServletResponse) {
        try {
            final MemberAndJwtDto dto = memberService.findMemberAndGenerateJwt(kakaoId);
            final MemberSigninSuccessResponse response = new MemberSigninSuccessResponse(Status.SUCCESS, dto.getMember(), dto.getAccessToken());
            putRefreshTokenToCookie(httpServletResponse, dto.getRefreshToken());

            return ResponseEntity.ok(ResultResponse.of(SIGNIN_SUCCESS, response));
        } catch (MemberNotFoundException e) {
            final MemberSigninFailResponse response = new MemberSigninFailResponse(Status.FAILURE);

            return ResponseEntity.ok(ResultResponse.of(SIGNIN_FAILURE, response));
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

    @ApiOperation(value = "이메일 검증")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "불필요", example = " "),
            @ApiImplicitParam(name = "email", value = "이메일", required = true, example = "12161542@inha.edu")
    })
    @PostMapping("/signup/check/email")
    public ResponseEntity<ResultResponse> checkEmail(@RequestParam String email) {
        final EmailCheckResponse response = memberService.checkEmail(email);

        return ResponseEntity.ok(ResultResponse.of(CHECK_EMAIL_SUCCESS, response));
    }
}
