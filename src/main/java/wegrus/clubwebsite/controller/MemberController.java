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
import wegrus.clubwebsite.dto.VerificationResponse;
import wegrus.clubwebsite.dto.member.*;
import wegrus.clubwebsite.dto.result.ResultResponse;
import wegrus.clubwebsite.service.MemberService;

import javax.mail.MessagingException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import static wegrus.clubwebsite.dto.result.ResultCode.*;

@Api(tags = "회원 API")
@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @ApiOperation(value = "본인 이메일 검증")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "불필요", example = " "),
            @ApiImplicitParam(name = "verificationKey", value = "인증키", required = true, example = "5ecc3d01-bd92-4f1a-bdf2-9a5a777871ae")
    })
    @PostMapping("/signup/verify")
    public ResponseEntity<ResultResponse> verifySchoolEmail(@RequestParam String verificationKey) {
        final VerificationResponse response = memberService.checkVerificationKey(verificationKey);

        return ResponseEntity.ok(ResultResponse.of(REQUEST_VERIFY_SUCCESS, response));
    }

    @ApiOperation(value = "회원 가입")
    @ApiImplicitParam(name = "Authorization", value = "불필요", example = " ")
    @PostMapping("/signup")
    public ResponseEntity<ResultResponse> signup(@Validated @RequestBody MemberSignupRequest request) throws MessagingException {
        final String verificationKey = memberService.validateAndSendVerificationMailAndSaveMember(request);
        final MemberSignupResponse response = new MemberSignupResponse(verificationKey);

        return ResponseEntity.ok(ResultResponse.of(SIGNUP_SUCCESS, response));
    }

    @ApiOperation(value = "로그인")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Authorization", value = "불필요", example = " "),
            @ApiImplicitParam(name = "kakaoId", value = "카카오 회원 번호", required = true, example = "123456789")
    })
    @PostMapping("/login")
    public ResponseEntity<ResultResponse> login(
            @Validated @NotNull(message = "카카오 회원 번호는 필수입니다.") @RequestParam Long kakaoId,
            HttpServletResponse httpServletResponse) {
        final MemberAndJwtDto dto = memberService.findMemberAndGenerateJwt(kakaoId);
        final MemberLoginResponse response = new MemberLoginResponse(dto.getMember(), dto.getAccessToken());
        putRefreshTokenToCookie(httpServletResponse, dto.getRefreshToken());

        return ResponseEntity.ok(ResultResponse.of(LOGIN_SUCCESS, response));
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
    @PostMapping("/members/logout")
    public ResponseEntity<ResultResponse> logout(@CookieValue(value = "refreshToken") Cookie cookie) {
        memberService.deleteRefreshToken(cookie.getValue());

        return ResponseEntity.ok(ResultResponse.of(LOGOUT_SUCCESS, null));
    }

    @ApiOperation(value = "토큰 재발급")
    @PostMapping("/reissue")
    public ResponseEntity<ResultResponse> reIssue(
            @RequestHeader("Authorization") String accessToken,
            @CookieValue(value = "refreshToken") Cookie cookie,
            HttpServletResponse httpServletResponse) {
        final JwtDto jwtDto = memberService.reIssueJwt(accessToken, cookie.getValue());
        putRefreshTokenToCookie(httpServletResponse, jwtDto.getRefreshToken());
        final ReIssueResponse response = new ReIssueResponse(jwtDto.getAccessToken());

        return ResponseEntity.ok(ResultResponse.of(REISSUE_SUCCESS, response));
    }
}
