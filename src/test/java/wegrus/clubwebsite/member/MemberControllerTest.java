package wegrus.clubwebsite.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import wegrus.clubwebsite.config.JwtRequestFilter;
import wegrus.clubwebsite.config.WebSecurityConfig;
import wegrus.clubwebsite.controller.MemberController;
import wegrus.clubwebsite.dto.Status;
import wegrus.clubwebsite.dto.VerificationResponse;
import wegrus.clubwebsite.dto.member.EmailCheckResponse;
import wegrus.clubwebsite.dto.member.JwtDto;
import wegrus.clubwebsite.dto.member.MemberAndJwtDto;
import wegrus.clubwebsite.dto.member.MemberSignupRequest;
import wegrus.clubwebsite.entity.member.Member;
import wegrus.clubwebsite.entity.member.MemberAcademicStatus;
import wegrus.clubwebsite.entity.member.MemberGrade;
import wegrus.clubwebsite.exception.MemberNotFoundException;
import wegrus.clubwebsite.service.MemberService;

import javax.servlet.http.Cookie;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static wegrus.clubwebsite.dto.error.ErrorCode.*;
import static wegrus.clubwebsite.dto.result.ResultCode.*;

@MockBean(JpaMetamodelMappingContext.class) // for JpaAuditing
@WebMvcTest(value = MemberController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {WebSecurityConfig.class, JwtRequestFilter.class})
})
@WithMockUser
public class MemberControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @MockBean
    private MemberService memberService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void init() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .apply(springSecurity())
                .build();
    }

    @Test
    @DisplayName("본인 이메일 인증 API: 성공")
    void verifySchoolEmail_success() throws Exception {
        // given
        final VerificationResponse response = new VerificationResponse(true, VERIFY_EMAIL_SUCCESS.getMessage());
        doReturn(response).when(memberService).checkVerificationKey(any(String.class));
        final String verificationKey = UUID.randomUUID().toString();

        // when
        final ResultActions perform = mockMvc.perform(
                post("/signup/verify").with(csrf())
                        .contentType(APPLICATION_FORM_URLENCODED)
                        .param("verificationKey", verificationKey)
        );

        // then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value(REQUEST_VERIFY_SUCCESS.getCode()))
                .andExpect(jsonPath("data.certified").value("true"))
                .andExpect(jsonPath("data.reason").value(VERIFY_EMAIL_SUCCESS.getMessage()));
    }

    @Test
    @DisplayName("본인 이메일 인증 API: 실패")
    void verifySchoolEmail_fail() throws Exception {
        // given
        final VerificationResponse response = new VerificationResponse(false, EXPIRED_VERIFICATION_KEY.getMessage());
        doReturn(response).when(memberService).checkVerificationKey(any(String.class));
        final String verificationKey = UUID.randomUUID().toString();

        // when
        final ResultActions perform = mockMvc.perform(
                post("/signup/verify").with(csrf())
                        .contentType(APPLICATION_FORM_URLENCODED)
                        .param("verificationKey", verificationKey)
        );

        // then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value(REQUEST_VERIFY_SUCCESS.getCode()))
                .andExpect(jsonPath("data.certified").value("false"))
                .andExpect(jsonPath("data.reason").value(EXPIRED_VERIFICATION_KEY.getMessage()));
    }

    @Test
    @DisplayName("본인 이메일 인증 API: 바인딩 예외")
    void verifySchoolEmail_bindEx() throws Exception {
        // given
        final String verificationKey = "";

        // when
        final ResultActions perform = mockMvc.perform(
                post("/signup/verify").with(csrf())
                        .contentType(APPLICATION_FORM_URLENCODED)
                        .param("verificationKey", verificationKey)
        );

        // then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value(INVALID_INPUT_VALUE.getCode()))
                .andExpect(jsonPath("message").value(INVALID_INPUT_VALUE.getMessage()))
                .andExpect(jsonPath("errors[0].field").value("verificationKey"))
                .andExpect(jsonPath("errors[0].value").value(verificationKey))
                .andExpect(jsonPath("errors[0].reason").value("인증 키는 필수입니다."));
    }

    @Test
    @DisplayName("회원 가입 API: 성공")
    void signup_success() throws Exception {
        // given
        final String verificationKey = UUID.randomUUID().toString();
        doReturn(verificationKey).when(memberService).validateAndSendVerificationMailAndSaveMember(any(MemberSignupRequest.class));
        final MemberSignupRequest request = new MemberSignupRequest("12161111@inha.edu", 123456789L, "홍길동", "컴퓨터공학과", "010-1234-1234", MemberAcademicStatus.ATTENDING, MemberGrade.SENIOR);

        // when
        final ResultActions perform = mockMvc.perform(
                post("/signup").with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );

        // then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value(SIGNUP_SUCCESS.getCode()))
                .andExpect(jsonPath("data.verificationKey").value(verificationKey));
    }

    @Test
    @DisplayName("회원 가입 API: 바인딩 예외")
    void signup_bindEx() throws Exception {
        // given
        final MemberSignupRequest request = new MemberSignupRequest("12161111@inha.edu", 123456789L, "홍길동", " ", "010-1234-1234", MemberAcademicStatus.ATTENDING, MemberGrade.SENIOR);
        final MemberSignupRequest request2 = new MemberSignupRequest("12161111@gmail.com", 123456789L, "홍길동", "컴퓨터공학과", "010-1234-1234", MemberAcademicStatus.ATTENDING, MemberGrade.SENIOR);
        final MemberSignupRequest request3 = new MemberSignupRequest("12161111@inha.edu", 123456789L, "홍길동", "컴퓨터공학과", "01012341234", MemberAcademicStatus.ATTENDING, MemberGrade.SENIOR);

        // when
        final ResultActions perform = mockMvc.perform(
                post("/signup").with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        );
        final ResultActions perform2 = mockMvc.perform(
                post("/signup").with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2))
        );
        final ResultActions perform3 = mockMvc.perform(
                post("/signup").with(csrf())
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request3))
        );

        // then
        perform
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value(INVALID_INPUT_VALUE.getCode()))
                .andExpect(jsonPath("message").value(INVALID_INPUT_VALUE.getMessage()))
                .andExpect(jsonPath("errors[0].field").value("department"))
                .andExpect(jsonPath("errors[0].value").value(" "))
                .andExpect(jsonPath("errors[0].reason").value("회원 학과는 필수입니다."));
        perform2
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value(INVALID_INPUT_VALUE.getCode()))
                .andExpect(jsonPath("message").value(INVALID_INPUT_VALUE.getMessage()))
                .andExpect(jsonPath("errors[0].field").value("email"))
                .andExpect(jsonPath("errors[0].value").value("12161111@gmail.com"))
                .andExpect(jsonPath("errors[0].reason").value("인하대학교 이메일 형식만 가능합니다."));

        perform3
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("code").value(INVALID_INPUT_VALUE.getCode()))
                .andExpect(jsonPath("message").value(INVALID_INPUT_VALUE.getMessage()))
                .andExpect(jsonPath("errors[0].field").value("phone"))
                .andExpect(jsonPath("errors[0].value").value("01012341234"))
                .andExpect(jsonPath("errors[0].reason").value("연락처는 010-1234-5678 형식으로 입력해주세요."));
    }

    @Test
    @DisplayName("로그인 API: 성공")
    void login_success() throws Exception {
        // given
        final Member member = new Member(123456789L, "12161111@inha.edu", "홍길동", "12161111", "컴퓨터공학과", MemberGrade.SENIOR, "010-1234-1234", MemberAcademicStatus.ATTENDING);
        final String accessToken = "accessToken";
        final String refreshToken = "refreshToken";
        final MemberAndJwtDto dto = new MemberAndJwtDto(member, accessToken, refreshToken);

        doReturn(dto).when(memberService).findMemberAndGenerateJwt(any(Long.class));

        // when
        final ResultActions perform = mockMvc.perform(
                post("/signin").with(csrf())
                        .contentType(APPLICATION_FORM_URLENCODED)
                        .param("kakaoId", "123456789")
        );

        // then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value(SIGNIN_SUCCESS.getCode()))
                .andExpect(jsonPath("message").value(SIGNIN_SUCCESS.getMessage()))
                .andExpect(jsonPath("data.status").value(Status.SUCCESS))
                .andExpect(jsonPath("data.member.kakaoId").value(123456789L))
                .andExpect(jsonPath("data.accessToken").value(accessToken))
                .andExpect(cookie().value("refreshToken", refreshToken))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().maxAge("refreshToken", 14 * 24 * 60 * 60));
    }

    @Test
    @DisplayName("로그인 API: 실패")
    void login_fail() throws Exception {
        // given
        doThrow(MemberNotFoundException.class).when(memberService).findMemberAndGenerateJwt(any(Long.class));

        // when
        final ResultActions perform = mockMvc.perform(
                post("/signin").with(csrf())
                        .contentType(APPLICATION_FORM_URLENCODED)
                        .param("kakaoId", "123456789")
        );

        // then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value(SIGNIN_FAILURE.getCode()))
                .andExpect(jsonPath("message").value(SIGNIN_FAILURE.getMessage()))
                .andExpect(jsonPath("data.status").value(Status.FAILURE));
    }

    @Test
    @DisplayName("로그인 API: 바인딩 예외")
    void login_bindEx() throws Exception {
        // given
        final Long kakaoId = null;

        // when
        final ResultActions perform = mockMvc.perform(
                post("/signin").with(csrf())
                        .contentType(APPLICATION_FORM_URLENCODED)
                        .param("kakaoId", String.valueOf(kakaoId))
        );

        // then
        perform
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그아웃 API: 성공")
    void signout_success() throws Exception {
        // given
        final String refreshToken = "refreshToken";
        final Cookie cookie = new Cookie("refreshToken", refreshToken);

        doNothing().when(memberService).deleteRefreshToken(any(String.class));

        // when
        final ResultActions perform = mockMvc.perform(
                post("/members/signout").with(csrf())
                        .cookie(cookie)
        );

        // then
        perform
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist("refreshToken"))
                .andExpect(jsonPath("code").value(SIGNOUT_SUCCESS.getCode()))
                .andExpect(jsonPath("message").value(SIGNOUT_SUCCESS.getMessage()));
    }

    @Test
    @DisplayName("토큰 재발급 API: 성공")
    void reIssue_success() throws Exception {
        // given
        final String accessToken = "accessToken";
        final String refreshToken = "refreshToken";
        final Cookie cookie = new Cookie("refreshToken", refreshToken);

        final JwtDto dto = new JwtDto(accessToken, refreshToken);
        doReturn(dto).when(memberService).reIssueJwt(any(String.class));

        // when
        final ResultActions perform = mockMvc.perform(
                post("/reissue").with(csrf())
                        .header("Authorization", accessToken)
                        .cookie(cookie)
        );

        // then
        perform
                .andExpect(status().isOk())
                .andExpect(cookie().value("refreshToken", refreshToken))
                .andExpect(cookie().httpOnly("refreshToken", true))
                .andExpect(cookie().maxAge("refreshToken", 14 * 24 * 60 * 60))
                .andExpect(jsonPath("code").value(REISSUE_SUCCESS.getCode()))
                .andExpect(jsonPath("message").value(REISSUE_SUCCESS.getMessage()))
                .andExpect(jsonPath("data.accessToken").value(accessToken));
    }

    @Test
    @DisplayName("이메일 검증 API: 성공")
    void checkEmail_success() throws Exception {
        // given
        final String email = "12161111@inha.edu";
        final EmailCheckResponse response = new EmailCheckResponse(Status.SUCCESS, VALID_EMAIL.getMessage());

        doReturn(response).when(memberService).checkEmail(any(String.class));

        // when
        final ResultActions perform = mockMvc.perform(
                post("/signup/check/email").with(csrf())
                        .contentType(APPLICATION_FORM_URLENCODED)
                        .param("email", email)
        );

        // then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value(CHECK_EMAIL_SUCCESS.getCode()))
                .andExpect(jsonPath("message").value(CHECK_EMAIL_SUCCESS.getMessage()))
                .andExpect(jsonPath("data.status").value(Status.SUCCESS))
                .andExpect(jsonPath("data.reason").value(VALID_EMAIL.getMessage()));
    }

    @Test
    @DisplayName("이메일 검증 API: 실패")
    void checkEmail_fail() throws Exception {
        // given
        final String email = "12161111@inha.edu";
        final String invalidEmail = "12161111@gmail.com";
        final EmailCheckResponse response = new EmailCheckResponse(Status.FAILURE, EMAIL_ALREADY_EXIST.getMessage());
        final EmailCheckResponse response2 = new EmailCheckResponse(Status.FAILURE, INVALID_EMAIL.getMessage());

        doReturn(response).when(memberService).checkEmail(email);
        doReturn(response2).when(memberService).checkEmail(invalidEmail);

        // when
        final ResultActions perform = mockMvc.perform(
                post("/signup/check/email").with(csrf())
                        .contentType(APPLICATION_FORM_URLENCODED)
                        .param("email", email)
        );
        final ResultActions perform2 = mockMvc.perform(
                post("/signup/check/email").with(csrf())
                        .contentType(APPLICATION_FORM_URLENCODED)
                        .param("email", invalidEmail)
        );

        // then
        perform
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value(CHECK_EMAIL_SUCCESS.getCode()))
                .andExpect(jsonPath("message").value(CHECK_EMAIL_SUCCESS.getMessage()))
                .andExpect(jsonPath("data.status").value(Status.FAILURE))
                .andExpect(jsonPath("data.reason").value(EMAIL_ALREADY_EXIST.getMessage()));
        perform2
                .andExpect(status().isOk())
                .andExpect(jsonPath("code").value(CHECK_EMAIL_SUCCESS.getCode()))
                .andExpect(jsonPath("message").value(CHECK_EMAIL_SUCCESS.getMessage()))
                .andExpect(jsonPath("data.status").value(Status.FAILURE))
                .andExpect(jsonPath("data.reason").value(INVALID_EMAIL.getMessage()));
    }
}
