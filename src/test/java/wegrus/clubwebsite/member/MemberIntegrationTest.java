package wegrus.clubwebsite.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import wegrus.clubwebsite.dto.Status;
import wegrus.clubwebsite.dto.VerificationResponse;
import wegrus.clubwebsite.dto.member.*;
import wegrus.clubwebsite.dto.result.ResultResponse;
import wegrus.clubwebsite.entity.member.MemberAcademicStatus;
import wegrus.clubwebsite.entity.member.MemberGrade;
import wegrus.clubwebsite.entity.member.MemberRoles;
import wegrus.clubwebsite.util.RedisUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static wegrus.clubwebsite.dto.result.ResultCode.VERIFY_EMAIL_SUCCESS;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class MemberIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisUtil redisUtil;

    public EmailCheckResponse checkEmailAPI(String email) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("email", email);

        final HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        final ResponseEntity<ResultResponse> response = restTemplate.postForEntity("/signup/check/email", request, ResultResponse.class);

        return objectMapper.convertValue(response.getBody().getData(), EmailCheckResponse.class);
    }

    public MemberSignupResponse signupAPI(String email, Long kakaoId, String name, String department, String phone, MemberAcademicStatus memberAcademicStatus, MemberGrade memberGrade) {
        final MemberSignupRequest memberSignupRequest = new MemberSignupRequest(email, kakaoId, name, department, phone, memberAcademicStatus, memberGrade);

        final HttpEntity<MemberSignupRequest> request = new HttpEntity<>(memberSignupRequest);
        final ResponseEntity<ResultResponse> response = restTemplate.postForEntity("/signup", request, ResultResponse.class);

        return objectMapper.convertValue(response.getBody().getData(), MemberSignupResponse.class);
    }

    public VerificationResponse verifySchoolEmailAPI(String verificationKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("verificationKey", verificationKey);

        final HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        final ResponseEntity<ResultResponse> response = restTemplate.postForEntity("/signup/verify", request, ResultResponse.class);

        return objectMapper.convertValue(response.getBody().getData(), VerificationResponse.class);
    }

    public ResponseEntity<ResultResponse> signinAPI(Long kakaoId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, Long> map = new LinkedMultiValueMap<>();
        map.add("kakaoId", kakaoId);

        final HttpEntity<MultiValueMap<String, Long>> request = new HttpEntity<>(map, headers);
        return restTemplate.exchange("/signin", HttpMethod.POST, request, ResultResponse.class);
    }

    public ResponseEntity<ResultResponse> signoutAPI(String accessToken, String cookie) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.COOKIE, cookie);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

        final HttpEntity<MultiValueMap<String, Long>> request = new HttpEntity<>(null, headers);
        return restTemplate.exchange("/members/signout", HttpMethod.POST, request, ResultResponse.class);
    }

    public ResponseEntity<ResultResponse> reissueAPI(String cookie) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.COOKIE, cookie);

        final HttpEntity<MultiValueMap<String, Long>> request = new HttpEntity<>(null, headers);
        return restTemplate.exchange("/reissue", HttpMethod.POST, request, ResultResponse.class);
    }

    @Test
    @DisplayName("이메일 검증")
    void checkEmail() throws Exception {
        // given
        final String email = "12345678@inha.edu";

        // when
        final EmailCheckResponse response = checkEmailAPI(email);

        // then
        assertThat(response.getStatus()).isEqualTo(Status.SUCCESS);
    }

    @Test
    @DisplayName("회원 가입")
    void signup() throws Exception {
        // given
        final String email = "12345679@inha.edu";
        checkEmailAPI(email);
        
        // when
        final MemberSignupResponse response = signupAPI(email, 123433789L, "홍길동", "컴퓨터공학과", "010-1234-1234", MemberAcademicStatus.ATTENDING, MemberGrade.FRESHMAN);
        final String verificationKey = response.getVerificationKey();
        redisUtil.delete(verificationKey);

        // then
        assertThat(response.getVerificationKey()).isNotBlank();
    }

    @Test
    @DisplayName("본인 이메일 인증")
    void verifyEmail() throws Exception {
        // given
        final String email = "12161542@inha.edu";
        checkEmailAPI(email);
        final MemberSignupResponse memberSignupResponse = signupAPI(email, 123456789L, "홍길동", "컴퓨터공학과", "010-1234-1234", MemberAcademicStatus.ATTENDING, MemberGrade.FRESHMAN);
        final String verificationKey = memberSignupResponse.getVerificationKey();

        // when
        final VerificationResponse response = verifySchoolEmailAPI(verificationKey);

        // then
        assertThat(response.isCertified()).isTrue();
        assertThat(response.getReason()).isEqualTo(VERIFY_EMAIL_SUCCESS.getMessage());
    }

    @Test
    @DisplayName("로그인")
    void signin() throws Exception {
        // given
        final String email = "12344279@inha.edu";
        final long kakaoId = 111456789L;
        checkEmailAPI(email);
        final MemberSignupResponse memberSignupResponse = signupAPI(email, kakaoId, "홍길동", "컴퓨터공학과", "010-1234-1234", MemberAcademicStatus.ATTENDING, MemberGrade.FRESHMAN);
        final String verificationKey = memberSignupResponse.getVerificationKey();
        verifySchoolEmailAPI(verificationKey);

        // when
        final ResponseEntity<ResultResponse> responseEntity = signinAPI(kakaoId);
        final MemberSigninSuccessResponse response = objectMapper.convertValue(responseEntity.getBody().getData(), MemberSigninSuccessResponse.class);

        // then
        System.out.println(responseEntity.getHeaders().get("Set-Cookie").get(0));
        assertThat(responseEntity.getHeaders().get("Set-Cookie").get(0)).isNotBlank();
        assertThat(response.getStatus()).isEqualTo(Status.SUCCESS);
        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getMember().getRoles().contains(MemberRoles.ROLE_CERTIFIED.name())).isTrue();
    }

    @Test
    @DisplayName("로그아웃")
    void signout() throws Exception {
        // given
        final String email = "12845679@inha.edu";
        final long kakaoId = 123477789L;
        checkEmailAPI(email);
        final MemberSignupResponse memberSignupResponse = signupAPI(email, kakaoId, "홍길동", "컴퓨터공학과", "010-1234-1234", MemberAcademicStatus.ATTENDING, MemberGrade.FRESHMAN);
        final String verificationKey = memberSignupResponse.getVerificationKey();
        verifySchoolEmailAPI(verificationKey);
        final ResponseEntity<ResultResponse> resultResponseResponseEntity = signinAPI(kakaoId);
        final String cookie = resultResponseResponseEntity.getHeaders().get("Set-Cookie").get(0);
        final MemberSigninSuccessResponse memberSigninSuccessResponse = objectMapper.convertValue(resultResponseResponseEntity.getBody().getData(), MemberSigninSuccessResponse.class);
        final String accessToken = memberSigninSuccessResponse.getAccessToken();

        // when
        final ResponseEntity<ResultResponse> responseEntity = signoutAPI(accessToken, cookie);
        final MemberSignoutResponse response = objectMapper.convertValue(responseEntity.getBody().getData(), MemberSignoutResponse.class);

        // then
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);
        assertThat(responseEntity.getHeaders().get("Set-Cookie")).isNull();
        assertThat(response.getStatus()).isEqualTo(Status.SUCCESS);
    }

    @Test
    @DisplayName("토큰 재발급")
    void reissue() throws Exception {
        // given
        final String email = "44345679@inha.edu";
        final long kakaoId = 123455789L;
        checkEmailAPI(email);
        final MemberSignupResponse memberSignupResponse = signupAPI(email, kakaoId, "홍길동", "컴퓨터공학과", "010-1234-1234", MemberAcademicStatus.ATTENDING, MemberGrade.FRESHMAN);
        final String verificationKey = memberSignupResponse.getVerificationKey();
        verifySchoolEmailAPI(verificationKey);
        final ResponseEntity<ResultResponse> resultResponseResponseEntity = signinAPI(kakaoId);
        final String cookie = resultResponseResponseEntity.getHeaders().get("Set-Cookie").get(0);
        
        // when
        final ResponseEntity<ResultResponse> responseEntity = reissueAPI(cookie);
        final ReIssueResponse response = objectMapper.convertValue(responseEntity.getBody().getData(), ReIssueResponse.class);
        redisUtil.delete(cookie.split(";")[0].substring(13));

        // then
        System.out.println(responseEntity.getHeaders().get("Set-Cookie").get(0));
        assertThat(responseEntity.getHeaders().get("Set-Cookie").get(0)).isNotBlank();
        System.out.println("accessToken=" + response.getAccessToken());
        assertThat(response.getAccessToken()).isNotBlank();
    }
}
