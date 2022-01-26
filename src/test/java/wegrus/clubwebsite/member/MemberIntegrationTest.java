package wegrus.clubwebsite.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import wegrus.clubwebsite.dto.Status;
import wegrus.clubwebsite.dto.StatusResponse;
import wegrus.clubwebsite.dto.VerificationResponse;
import wegrus.clubwebsite.dto.member.*;
import wegrus.clubwebsite.dto.result.ResultResponse;
import wegrus.clubwebsite.entity.member.Member;
import wegrus.clubwebsite.entity.member.MemberAcademicStatus;
import wegrus.clubwebsite.entity.member.MemberGrade;
import wegrus.clubwebsite.entity.member.MemberRoles;
import wegrus.clubwebsite.repository.MemberRepository;
import wegrus.clubwebsite.util.AmazonS3Util;
import wegrus.clubwebsite.util.KakaoUtil;
import wegrus.clubwebsite.util.RedisUtil;
import wegrus.clubwebsite.vo.Image;

import java.io.*;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static wegrus.clubwebsite.dto.result.ResultCode.VERIFY_EMAIL_SUCCESS;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional
public class MemberIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisUtil redisUtil;

    @MockBean
    private KakaoUtil kakaoUtil;

    @MockBean
    private AmazonS3Util amazonS3Util;

    @BeforeEach
    void init() throws IOException {
        doReturn(UUID.randomUUID().toString()).when(kakaoUtil).getAccessTokenFromKakaoAPI(any(String.class));
        doNothing().when(amazonS3Util).deleteImage(any(Image.class), any(String.class));
        doReturn(Image.builder().url("new url").build()).when(amazonS3Util).uploadImage(any(MultipartFile.class), any(String.class));
    }

    public EmailCheckResponse checkEmailAPI(String email) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("email", email);

        final HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        final ResponseEntity<ResultResponse> response = restTemplate.postForEntity("/signup/check/email", request, ResultResponse.class);

        return objectMapper.convertValue(response.getBody().getData(), EmailCheckResponse.class);
    }

    public MemberSignupResponse signupAPI(String email, String name, String department, String phone, MemberAcademicStatus memberAcademicStatus, MemberGrade memberGrade, String userId) {
        final MemberSignupRequest memberSignupRequest = new MemberSignupRequest(email, name, department, phone, memberAcademicStatus, memberGrade, userId);

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

    public ResponseEntity<ResultResponse> signinAPI(String authorizationCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("authorizationCode", authorizationCode);

        final HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
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

    public MemberInfoResponse getInfoAPI(String accessToken, Long memberId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

        final HttpEntity<MultiValueMap<String, Long>> request = new HttpEntity<>(null, headers);
        final ResponseEntity<ResultResponse> responseEntity = restTemplate.exchange("/members/info/" + memberId, HttpMethod.GET, request, ResultResponse.class);
        return objectMapper.convertValue(responseEntity.getBody().getData(), MemberInfoResponse.class);
    }

    public MemberInfoUpdateResponse updateInfoAPI(String accessToken, MemberInfoUpdateRequest memberInfoUpdateRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

        final HttpEntity<MemberInfoUpdateRequest> request = new HttpEntity<>(memberInfoUpdateRequest, headers);
        final ResponseEntity<ResultResponse> responseEntity = restTemplate.exchange("/members/info", HttpMethod.PATCH, request, ResultResponse.class);
        return objectMapper.convertValue(responseEntity.getBody().getData(), MemberInfoUpdateResponse.class);
    }

    public MemberImageUpdateResponse updateImageAPI(String accessToken, Resource resource) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", resource);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        final ResponseEntity<ResultResponse> responseEntity = restTemplate.exchange("/members/image", HttpMethod.PATCH, requestEntity, ResultResponse.class);
        return objectMapper.convertValue(responseEntity.getBody().getData(), MemberImageUpdateResponse.class);
    }

    public ValidateEmailResponse validateEmailAPI(String email){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        final HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(null, headers);
        final ResponseEntity<ResultResponse> responseEntity = restTemplate.exchange("/signup/validate/email?email=" + email, HttpMethod.GET, requestEntity, ResultResponse.class);
        return objectMapper.convertValue(responseEntity.getBody().getData(), ValidateEmailResponse.class);
    }

    public RequestAuthorityResponse requestAuthorityAPI(String accessToken, MemberRoles memberRoles) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, MemberRoles> map = new LinkedMultiValueMap<>();
        map.add("role", memberRoles);

        final HttpEntity<MultiValueMap<String, MemberRoles>> requestEntity = new HttpEntity<>(map, headers);
        final ResponseEntity<ResultResponse> responseEntity = restTemplate.exchange("/members/authority", HttpMethod.POST, requestEntity, ResultResponse.class);
        return objectMapper.convertValue(responseEntity.getBody().getData(), RequestAuthorityResponse.class);
    }

    public StatusResponse resignAPI(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);

        final HttpEntity<MultiValueMap<String, MemberRoles>> requestEntity = new HttpEntity<>(null, headers);
        final ResponseEntity<ResultResponse> responseEntity = restTemplate.exchange("/members/resign", HttpMethod.POST, requestEntity, ResultResponse.class);
        return objectMapper.convertValue(responseEntity.getBody().getData(), StatusResponse.class);
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

        // when
        final MemberSignupResponse response = signupAPI(email, "홍길동", "컴퓨터공학과", "010-1234-1234", MemberAcademicStatus.ATTENDING, MemberGrade.FRESHMAN, "123433789L");

        // then
        assertThat(response.getMember()).isNotNull();
    }

    @Test
    @DisplayName("본인 이메일 인증")
    void verifyEmail() throws Exception {
        // given
        final String email = "12161542@inha.edu";
        final EmailCheckResponse emailCheckResponse = checkEmailAPI(email);

        // when
        final VerificationResponse response = verifySchoolEmailAPI(emailCheckResponse.getVerificationKey());

        // then
        assertThat(response.isCertified()).isTrue();
        assertThat(response.getReason()).isEqualTo(VERIFY_EMAIL_SUCCESS.getMessage());
    }

    @Test
    @DisplayName("이메일 인증 여부 확인")
    void validateEmail() throws Exception {
        // given
        final String email = "19991332@inha.edu";
        final EmailCheckResponse emailCheckResponse = checkEmailAPI(email);
        verifySchoolEmailAPI(emailCheckResponse.getVerificationKey());

        // when
        final ValidateEmailResponse response = validateEmailAPI(email);

        // then
        assertThat(response.getStatus()).isEqualTo(Status.SUCCESS);
    }

    @Test
    @DisplayName("로그인")
    void signin() throws Exception {
        // given
        final String email = "12344279@inha.edu";
        final String userId = "111456789L";
        doReturn(userId).when(kakaoUtil).getUserIdFromKakaoAPI(any(String.class));
        signupAPI(email, "홍길동", "컴퓨터공학과", "010-1234-1234", MemberAcademicStatus.ATTENDING, MemberGrade.FRESHMAN, userId);

        // when
        final ResponseEntity<ResultResponse> responseEntity = signinAPI("authorizationCode");
        final MemberSigninSuccessResponse response = objectMapper.convertValue(responseEntity.getBody().getData(), MemberSigninSuccessResponse.class);

        // then
        System.out.println(responseEntity.getHeaders().get("Set-Cookie").get(0));
        assertThat(responseEntity.getHeaders().get("Set-Cookie").get(0)).isNotBlank();
        assertThat(response.getStatus()).isEqualTo(Status.SUCCESS);
        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getMember().getRoles().contains(MemberRoles.ROLE_GUEST.name())).isTrue();
    }

    @Test
    @DisplayName("로그아웃")
    void signout() throws Exception {
        // given
        final String email = "12845679@inha.edu";
        final String userId = "123477789L";
        doReturn(userId).when(kakaoUtil).getUserIdFromKakaoAPI(any(String.class));
        signupAPI(email, "홍길동", "컴퓨터공학과", "010-1234-1234", MemberAcademicStatus.ATTENDING, MemberGrade.FRESHMAN, userId);
        final ResponseEntity<ResultResponse> resultResponseResponseEntity = signinAPI("authorizationCode");
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
        final String userId = "123455789L";
        doReturn(userId).when(kakaoUtil).getUserIdFromKakaoAPI(any(String.class));
        signupAPI(email, "홍길동", "컴퓨터공학과", "010-1234-1234", MemberAcademicStatus.ATTENDING, MemberGrade.FRESHMAN, userId);
        final ResponseEntity<ResultResponse> resultResponseResponseEntity = signinAPI("authorizationCode");
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

    @Test
    @DisplayName("회원 정보 조회")
    void getInfo() throws Exception {
        // given
        final String email = "12344379@inha.edu";
        final String userId = "111456389L";
        doReturn(userId).when(kakaoUtil).getUserIdFromKakaoAPI(any(String.class));
        signupAPI(email, "홍길동", "컴퓨터공학과", "010-1234-1234", MemberAcademicStatus.ATTENDING, MemberGrade.FRESHMAN, userId);
        final ResponseEntity<ResultResponse> responseEntity = signinAPI("authorizationCode");
        final MemberSigninSuccessResponse memberSigninSuccessResponse = objectMapper.convertValue(responseEntity.getBody().getData(), MemberSigninSuccessResponse.class);
        final String accessToken = memberSigninSuccessResponse.getAccessToken();

        final String email2 = "12499300@inha.edu";
        final String userId2 = "111222389L";
        doReturn(userId2).when(kakaoUtil).getUserIdFromKakaoAPI(any(String.class));
        signupAPI(email2, "홍길동", "컴퓨터공학과", "010-1234-1234", MemberAcademicStatus.ATTENDING, MemberGrade.FRESHMAN, userId2);
        final ResponseEntity<ResultResponse> responseEntity2 = signinAPI("authorizationCode");
        final MemberSigninSuccessResponse memberSigninSuccessResponse2 = objectMapper.convertValue(responseEntity2.getBody().getData(), MemberSigninSuccessResponse.class);

        // when
        final MemberInfoResponse response = getInfoAPI(accessToken, memberSigninSuccessResponse.getMember().getId());
        final MemberDto info = objectMapper.convertValue(response.getInfo(), MemberDto.class);

        final MemberInfoResponse response2 = getInfoAPI(accessToken, memberSigninSuccessResponse2.getMember().getId());
        final MemberSimpleDto info2 = objectMapper.convertValue(response2.getInfo(), MemberSimpleDto.class);

        // then
        assertThat(info.getStudentId()).isEqualTo("12344379");
        assertThat(info2.getStudentId()).isEqualTo("49");
    }

    @Test
    @DisplayName("회원 정보 수정")
    void updateInfo() throws Exception {
        // given
        final String email = "24344311@inha.edu";
        final String userId = "151456339L";
        doReturn(userId).when(kakaoUtil).getUserIdFromKakaoAPI(any(String.class));
        signupAPI(email, "홍길동", "컴퓨터공학과", "010-1234-1234", MemberAcademicStatus.ATTENDING, MemberGrade.FRESHMAN, userId);
        final ResponseEntity<ResultResponse> responseEntity = signinAPI("authorizationCode");
        final MemberSigninSuccessResponse memberSigninSuccessResponse = objectMapper.convertValue(responseEntity.getBody().getData(), MemberSigninSuccessResponse.class);
        final String accessToken = memberSigninSuccessResponse.getAccessToken();
        final MemberInfoUpdateRequest request = new MemberInfoUpdateRequest("만두", "정통", "010-1234-1234", MemberAcademicStatus.ABSENCE, MemberGrade.FRESHMAN, "안녕");

        // when
        final MemberInfoUpdateResponse response = updateInfoAPI(accessToken, request);

        // then
        assertThat(response.getName()).isEqualTo("만두");
        assertThat(response.getDepartment()).isEqualTo("정통");
        assertThat(response.getAcademicStatus()).isEqualTo(MemberAcademicStatus.ABSENCE);
        assertThat(response.getIntroduce()).isEqualTo("안녕");
    }

    @Test
    @DisplayName("회원 이미지 변경")
    void updateImage() throws Exception {
        // given
        final String email = "24999911@inha.edu";
        final String userId = "12904812940";
        doReturn(userId).when(kakaoUtil).getUserIdFromKakaoAPI(any(String.class));
        signupAPI(email, "홍길동", "컴퓨터공학과", "010-1234-1234", MemberAcademicStatus.ATTENDING, MemberGrade.FRESHMAN, userId);
        final ResponseEntity<ResultResponse> responseEntity = signinAPI("authorizationCode");
        final MemberSigninSuccessResponse memberSigninSuccessResponse = objectMapper.convertValue(responseEntity.getBody().getData(), MemberSigninSuccessResponse.class);
        final String accessToken = memberSigninSuccessResponse.getAccessToken();
        final Resource resource = new FileSystemResource("src/test/resources/static/image.jpg");

        // when
        final MemberImageUpdateResponse response = updateImageAPI(accessToken, resource);

        // then
        assertThat(response.getImageUrl()).isEqualTo("new url");
        assertThat(response.getStatus()).isEqualTo(Status.SUCCESS);
    }

    @Test
    @DisplayName("회원 권한 요청")
    void requestAuthority() throws Exception {
        // given
        final String email = "33339911@inha.edu";
        final String userId = "122351124940";
        doReturn(userId).when(kakaoUtil).getUserIdFromKakaoAPI(any(String.class));
        signupAPI(email, "홍길동", "컴퓨터공학과", "010-1234-1234", MemberAcademicStatus.ATTENDING, MemberGrade.FRESHMAN, userId);
        final ResponseEntity<ResultResponse> responseEntity = signinAPI("authorizationCode");
        final MemberSigninSuccessResponse memberSigninSuccessResponse = objectMapper.convertValue(responseEntity.getBody().getData(), MemberSigninSuccessResponse.class);
        final String accessToken = memberSigninSuccessResponse.getAccessToken();
        final MemberRoles memberRoles = MemberRoles.ROLE_MEMBER;

        // when
        final RequestAuthorityResponse response = requestAuthorityAPI(accessToken, memberRoles);

        // then
        assertThat(response.getStatus()).isEqualTo(Status.SUCCESS);
        assertThat(response.getRole()).isEqualTo(memberRoles);
    }

    @Test
    @DisplayName("회원 탈퇴")
    void resign() throws Exception {
        // given
        final String email = "33339922@inha.edu";
        final String userId = "1223511210";
        doReturn(userId).when(kakaoUtil).getUserIdFromKakaoAPI(any(String.class));
        signupAPI(email, "홍길동", "컴퓨터공학과", "010-1234-1234", MemberAcademicStatus.ATTENDING, MemberGrade.FRESHMAN, userId);
        final ResponseEntity<ResultResponse> responseEntity = signinAPI("authorizationCode");
        final MemberSigninSuccessResponse memberSigninSuccessResponse = objectMapper.convertValue(responseEntity.getBody().getData(), MemberSigninSuccessResponse.class);
        final String accessToken = memberSigninSuccessResponse.getAccessToken();

        // when
        final StatusResponse response = resignAPI(accessToken);

        // then
        assertThat(response.getStatus()).isEqualTo(Status.SUCCESS);
    }

    @Test
    @DisplayName("회원 재가입")
    void rejoin() throws Exception {
        // given
        final String email = "33355922@inha.edu";
        final String userId = "1223511210";
        doReturn(userId).when(kakaoUtil).getUserIdFromKakaoAPI(any(String.class));
        signupAPI(email, "홍길동", "컴퓨터공학과", "010-1234-1234", MemberAcademicStatus.ATTENDING, MemberGrade.FRESHMAN, userId);
        final Member member = memberRepository.findByEmail(email).get();
        final ResponseEntity<ResultResponse> responseEntity = signinAPI("authorizationCode");
        final MemberSigninSuccessResponse memberSigninSuccessResponse = objectMapper.convertValue(responseEntity.getBody().getData(), MemberSigninSuccessResponse.class);
        final String accessToken = memberSigninSuccessResponse.getAccessToken();
        resignAPI(accessToken);
        final String newEmail = "12341234@inha.edu";

        // when
        signupAPI(newEmail, "홍길동", "컴퓨터공학과", "010-1234-1234", MemberAcademicStatus.ATTENDING, MemberGrade.FRESHMAN, userId);
        final Member rejoinMember = memberRepository.findByEmail(newEmail).get();

        // then
        assertThat(member.getId()).isEqualTo(rejoinMember.getId());
        assertThat(rejoinMember.getRoles().size()).isEqualTo(1);
    }
}
