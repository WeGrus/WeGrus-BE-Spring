package wegrus.clubwebsite.member;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import wegrus.clubwebsite.dto.Status;
import wegrus.clubwebsite.dto.VerificationResponse;
import wegrus.clubwebsite.dto.member.*;
import wegrus.clubwebsite.entity.member.*;
import wegrus.clubwebsite.exception.MemberAlreadyExistException;
import wegrus.clubwebsite.exception.MemberNotFoundException;
import wegrus.clubwebsite.exception.RefreshTokenExpiredException;
import wegrus.clubwebsite.repository.MemberRepository;
import wegrus.clubwebsite.repository.MemberRoleRepository;
import wegrus.clubwebsite.repository.RoleRepository;
import wegrus.clubwebsite.service.MailService;
import wegrus.clubwebsite.service.MemberService;
import wegrus.clubwebsite.util.JwtTokenUtil;
import wegrus.clubwebsite.util.JwtUserDetailsUtil;
import wegrus.clubwebsite.util.RedisUtil;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private MemberRoleRepository memberRoleRepository;

    @Mock
    private MailService mailService;

    @Mock
    private RedisUtil redisUtil;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @Mock
    private JwtUserDetailsUtil jwtUserDetailsUtil;

    @InjectMocks
    private MemberService memberService;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(memberService, "VERIFICATION_KEY_VALID_TIME", 30);
        ReflectionTestUtils.setField(memberService, "REFRESH_TOKEN_VALID_TIME", 20000);

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add((GrantedAuthority) () -> "ROLE_GUEST");
        User user = new User("username", "password", authorities);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user,null);
        auth.setDetails(user);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("인증 키 검증: 성공")
    void checkVerificationKey_success() throws Exception {
        // given
        final String email = "12161111@inha.edu";
        doReturn(email).when(redisUtil).get(any(String.class));
        doReturn(true).when(redisUtil).delete(any(String.class));
        doReturn(Optional.of(new Role(MemberRoles.ROLE_CERTIFIED.name()))).when(roleRepository).findByName(any(String.class));
        final Optional<Member> member = Optional.of(new Member(123456789L, "12161111@inha.edu", "홍길동", "컴퓨터공학과", MemberGrade.SENIOR, "010-1234-1234", MemberAcademicStatus.ATTENDING));
        doReturn(member).when(memberRepository).findByEmail(any(String.class));

        // when
        final VerificationResponse response = memberService.checkVerificationKey(UUID.randomUUID().toString());

        // then
        assertThat(response.isCertified()).isTrue();
    }

    @Test
    @DisplayName("인증 키 검증: 실패")
    void checkVerificationKey_fail() throws Exception {
        // given
        doReturn(null).when(redisUtil).get(any(String.class));

        // when
        final VerificationResponse response = memberService.checkVerificationKey(UUID.randomUUID().toString());

        // then
        assertThat(response.isCertified()).isFalse();
    }

    @Test
    @DisplayName("회원가입 데이터 검증 & 인증메일 전송 & 회원 저장: 성공")
    void validateAndSendVerificationMailAndSaveMember_success() throws Exception {
        // given
        final MemberSignupRequest request = new MemberSignupRequest("12161111@inha.edu", 123456789L, "홍길동", "컴퓨터공학과", "010-1234-1234", MemberAcademicStatus.ATTENDING, MemberGrade.SENIOR);
        doReturn(Optional.empty()).when(memberRepository).findByKakaoIdOrEmail(any(Long.class), any(String.class));
        final Optional<Member> member = Optional.of(new Member(123456789L, "12161111@inha.edu", "홍길동", "컴퓨터공학과", MemberGrade.SENIOR, "010-1234-1234", MemberAcademicStatus.ATTENDING));
        doReturn(member.get()).when(memberRepository).save(any(Member.class));
        doNothing().when(mailService).sendSchoolMailVerification(any(String.class), any(String.class));
        doNothing().when(redisUtil).set(any(String.class), any(Object.class), any(Integer.class));
        doReturn(Optional.of(new Role(MemberRoles.ROLE_GUEST.name()))).when(roleRepository).findByName(any(String.class));

        // when
        final MemberSignupResponse response = memberService.validateAndSendVerificationMailAndSaveMember(request);

        // then
        assertThat(response.getVerificationKey()).isNotBlank();
    }

    @Test
    @DisplayName("회원가입 데이터 검증 & 인증메일 전송 & 회원 저장: 실패")
    void validateAndSendVerificationMailAndSaveMember_fail() throws Exception {
        // given
        final MemberSignupRequest request = new MemberSignupRequest("12161111@inha.edu", 123456789L, "홍길동", "컴퓨터공학과", "010-1234-1234", MemberAcademicStatus.ATTENDING, MemberGrade.SENIOR);
        final Optional<Member> member = Optional.of(new Member(123456789L, "12161111@inha.edu", "홍길동", "컴퓨터공학과", MemberGrade.SENIOR, "010-1234-1234", MemberAcademicStatus.ATTENDING));
        doReturn(member).when(memberRepository).findByKakaoIdOrEmail(any(Long.class), any(String.class));

        // when
        final Executable executable = () -> memberService.validateAndSendVerificationMailAndSaveMember(request);

        // then
        assertThrows(MemberAlreadyExistException.class, executable);
    }

    @Test
    @DisplayName("회원 조회 & JWT 생성: 성공")
    void findMemberAndGenerateJwt_success() throws Exception {
        // given
        final Optional<Member> member = Optional.of(new Member(123456789L, "12161111@inha.edu", "홍길동", "컴퓨터공학과", MemberGrade.SENIOR, "010-1234-1234", MemberAcademicStatus.ATTENDING));
        doReturn(member).when(memberRepository).findByKakaoId(any(Long.class));

        List<GrantedAuthority> authorities = new ArrayList<>();
        final User user = new User(String.valueOf(member.get().getId()), UUID.randomUUID().toString(), new ArrayList<>(authorities));
        doReturn(user).when(jwtUserDetailsUtil).loadUserByUsername(any(String.class));

        ReflectionTestUtils.setField(member.get(), "id", 1L);
        doReturn("accessToken").when(jwtTokenUtil).generateAccessToken(any(UserDetails.class));
        doReturn("refreshToken").when(jwtTokenUtil).generateRefreshToken(any(UserDetails.class));
        doNothing().when(redisUtil).set(any(String.class), any(Long.class), any(Integer.class));

        // when
        final MemberAndJwtDto memberAndJwtDto = memberService.findMemberAndGenerateJwt(123456789L);

        // then
        assertThat(memberAndJwtDto.getMember().getEmail()).isEqualTo(member.get().getEmail());
    }

    @Test
    @DisplayName("회원 조회 & JWT 생성: 실패")
    void findMemberAndGenerateJwt_fail() throws Exception {
        // given
        doReturn(Optional.empty()).when(memberRepository).findByKakaoId(any(Long.class));

        // when
        final Executable executable = () -> memberService.findMemberAndGenerateJwt(123456789L);

        // then
        assertThrows(MemberNotFoundException.class, executable);
    }

    @Test
    @DisplayName("JWT 토큰 재발급: 성공")
    void reIssueJwt_success() throws Exception {
        // given
        doNothing().when(jwtTokenUtil).validateRefreshToken(any(String.class));
        doReturn(true).when(redisUtil).delete(any(String.class));

        doReturn("username").when(jwtTokenUtil).getUsernameFromRefreshToken(any(String.class));
        doReturn(new User("usernme", "password", Collections.EMPTY_LIST)).when(jwtUserDetailsUtil).loadUserByUsername(any(String.class));
        doReturn("accessToken").when(jwtTokenUtil).generateAccessToken(any(UserDetails.class));
        doReturn("refreshToken").when(jwtTokenUtil).generateRefreshToken(any(UserDetails.class));
        doNothing().when(redisUtil).set(any(String.class), any(String.class), any(Integer.class));

        // when
        final JwtDto jwtDto = memberService.reIssueJwt("expiredToken");

        // then
        assertThat(jwtDto.getAccessToken()).isEqualTo("accessToken");
        assertThat(jwtDto.getRefreshToken()).isEqualTo("refreshToken");
    }

    @Test
    @DisplayName("JWT 토큰 재발급: 실패")
    void reIssueJwt_fail() throws Exception {
        // given
        doThrow(RefreshTokenExpiredException.class).when(jwtTokenUtil).validateRefreshToken(any(String.class));

        // when
        final Executable executable = () -> memberService.reIssueJwt("expiredToken");

        // then
        assertThrows(RefreshTokenExpiredException.class, executable);
    }

    @Test
    @DisplayName("이메일 검증: 성공")
    void checkEmail_success() throws Exception {
        // given
        doReturn(Optional.empty()).when(memberRepository).findByEmail(any(String.class));

        // when
        final EmailCheckResponse response = memberService.checkEmail("12161111@inha.edu");

        // then
        assertThat(response.getStatus()).isEqualTo(Status.SUCCESS);
    }

    @Test
    @DisplayName("이메일 검증: 실패")
    void checkEmail_fail() throws Exception {
        // given
        final Optional<Member> member = Optional.of(new Member(123456789L, "12161111@inha.edu", "홍길동", "컴퓨터공학과", MemberGrade.SENIOR, "010-1234-1234", MemberAcademicStatus.ATTENDING));
        doReturn(member).when(memberRepository).findByEmail(any(String.class));

        // when
        final EmailCheckResponse response = memberService.checkEmail("12161111@inha.edu");

        // then
        assertThat(response.getStatus()).isEqualTo(Status.FAILURE);
    }
}
