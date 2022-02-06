package wegrus.clubwebsite.member;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import wegrus.clubwebsite.dto.Status;
import wegrus.clubwebsite.dto.StatusResponse;
import wegrus.clubwebsite.dto.VerificationResponse;
import wegrus.clubwebsite.dto.member.*;
import wegrus.clubwebsite.entity.member.*;
import wegrus.clubwebsite.exception.*;
import wegrus.clubwebsite.repository.MemberRepository;
import wegrus.clubwebsite.repository.MemberRoleRepository;
import wegrus.clubwebsite.repository.RoleRepository;
import wegrus.clubwebsite.service.CertificationCodeInvalidException;
import wegrus.clubwebsite.service.MailService;
import wegrus.clubwebsite.service.MemberService;
import wegrus.clubwebsite.util.*;
import wegrus.clubwebsite.vo.Image;

import java.io.FileInputStream;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static wegrus.clubwebsite.dto.result.ResultCode.EMAIL_IS_VERIFIED;
import static wegrus.clubwebsite.dto.result.ResultCode.EMAIL_NOT_VERIFIED;

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

    @Mock
    private AmazonS3Util amazonS3Util;

    @Mock
    private ImageUtil imageUtil;

    @Mock
    private KakaoUtil kakaoUtil;

    @InjectMocks
    private MemberService memberService;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(memberService, "VERIFICATION_KEY_VALID_TIME", 30);
        ReflectionTestUtils.setField(memberService, "REFRESH_TOKEN_VALID_TIME", 20000);

        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add((GrantedAuthority) () -> "ROLE_GUEST");
        User user = new User("1", "password", authorities);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("인증 키 검증: 성공")
    void checkVerificationKey_success() throws Exception {
        // given
        final String email = "12161111@inha.edu";
        doReturn(email).when(redisUtil).get(any(String.class));
        doReturn(true).when(redisUtil).delete(any(String.class));
        doNothing().when(redisUtil).set(any(String.class), any(Object.class), any(Integer.class));

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
    @DisplayName("회원가입: 성공")
    void validateAndSaveMember_success() throws Exception {
        // given
        final MemberSignupRequest request = new MemberSignupRequest("12161111@inha.edu", "홍길동", "컴퓨터공학과", "010-1234-1234", MemberAcademicStatus.ATTENDING, MemberGrade.SENIOR, "token", Gender.MAN);
        doReturn(Optional.empty()).when(memberRepository).findByUserIdOrEmail(any(String.class), any(String.class));
        final Optional<Member> member = Optional.of(new Member("123456789L", "12161111@inha.edu", "홍길동", "컴퓨터공학과", MemberGrade.SENIOR, "010-1234-1234", MemberAcademicStatus.ATTENDING, Gender.MAN));
        doReturn(member.get()).when(memberRepository).save(any(Member.class));
        doReturn(Optional.of(new Role(MemberRoles.ROLE_GUEST.name()))).when(roleRepository).findByName(any(String.class));
        doNothing().when(amazonS3Util).createDirectory(any(String.class));

        // when
        final MemberSignupResponse response = memberService.validateAndSaveMember(request);

        // then
        assertThat(response.getMember()).isNotNull();
    }

    @Test
    @DisplayName("회원 재가입: 성공")
    void validateAndSaveMember_success2() throws Exception {
        // given
        final MemberSignupRequest request = new MemberSignupRequest("12161111@inha.edu", "홍길동", "컴퓨터공학과", "010-1234-1234", MemberAcademicStatus.ATTENDING, MemberGrade.SENIOR, "123456789L", Gender.MAN);
        final Optional<Member> member = Optional.of(new Member("123456789L", "12161111@inha.edu", "홍길동", "컴퓨터공학과", MemberGrade.SENIOR, "010-1234-1234", MemberAcademicStatus.ATTENDING, Gender.MAN));
        ReflectionTestUtils.setField(member.get(), "id", 1L);
        doReturn(member).when(memberRepository).findByUserId(any(String.class));

        final Role roleResign = new Role(MemberRoles.ROLE_RESIGN.name());
        ReflectionTestUtils.setField(roleResign, "id", 1L);
        doReturn(Optional.of(roleResign)).when(roleRepository).findByName(MemberRoles.ROLE_RESIGN.name());
        final Role roleBan = new Role(MemberRoles.ROLE_BAN.name());
        ReflectionTestUtils.setField(roleBan, "id", 2L);
        doReturn(Optional.of(roleBan)).when(roleRepository).findByName(MemberRoles.ROLE_BAN.name());

        final MemberRole memberRole = new MemberRole(member.get(), roleResign);
        ReflectionTestUtils.setField(memberRole, "id", 1L);
        doReturn(Optional.of(memberRole)).when(memberRoleRepository).findByMemberIdAndRoleId(any(Long.class), any(Long.class));
        doNothing().when(memberRoleRepository).deleteById(any(Long.class));

        final Role roleGuest = new Role(MemberRoles.ROLE_GUEST.name());
        ReflectionTestUtils.setField(roleGuest, "id", 3L);
        doReturn(Optional.of(roleGuest)).when(roleRepository).findByName(MemberRoles.ROLE_GUEST.name());
        doReturn(null).when(memberRoleRepository).save(any(MemberRole.class));

        // when
        final MemberSignupResponse response = memberService.validateAndSaveMember(request);

        // then
        assertThat(response.getMember()).isNotNull();
    }

    @Test
    @DisplayName("회원가입: 실패")
    void validateAndSaveMember_fail() throws Exception {
        // given
        final MemberSignupRequest request = new MemberSignupRequest("12161111@inha.edu", "홍길동", "컴퓨터공학과", "010-1234-1234", MemberAcademicStatus.ATTENDING, MemberGrade.SENIOR, "token", Gender.MAN);
        final Optional<Member> member = Optional.of(new Member("123456789L", "12161111@inha.edu", "홍길동", "컴퓨터공학과", MemberGrade.SENIOR, "010-1234-1234", MemberAcademicStatus.ATTENDING, Gender.MAN));
        doReturn(member).when(memberRepository).findByUserIdOrEmail(any(String.class), any(String.class));

        // when
        final Executable executable = () -> memberService.validateAndSaveMember(request);

        // then
        assertThrows(MemberAlreadyExistException.class, executable);
    }


    @Test
    @DisplayName("회원 재가입: 실패")
    void validateAndSaveMember_fail2() throws Exception {
        // given
        final MemberSignupRequest request = new MemberSignupRequest("12161111@inha.edu", "홍길동", "컴퓨터공학과", "010-1234-1234", MemberAcademicStatus.ATTENDING, MemberGrade.SENIOR, "123456789L", Gender.MAN);
        final Optional<Member> member = Optional.of(new Member("123456789L", "12161111@inha.edu", "홍길동", "컴퓨터공학과", MemberGrade.SENIOR, "010-1234-1234", MemberAcademicStatus.ATTENDING, Gender.MAN));
        ReflectionTestUtils.setField(member.get(), "id", 1L);
        doReturn(member).when(memberRepository).findByUserId(any(String.class));

        final Role roleResign = new Role(MemberRoles.ROLE_RESIGN.name());
        ReflectionTestUtils.setField(roleResign, "id", 1L);
        doReturn(Optional.of(roleResign)).when(roleRepository).findByName(MemberRoles.ROLE_RESIGN.name());
        final Role roleBan = new Role(MemberRoles.ROLE_BAN.name());
        ReflectionTestUtils.setField(roleBan, "id", 2L);
        doReturn(Optional.of(roleBan)).when(roleRepository).findByName(MemberRoles.ROLE_BAN.name());

        doReturn(Optional.empty()).when(memberRoleRepository).findByMemberIdAndRoleId(member.get().getId(), roleResign.getId());
        doReturn(Optional.of(new MemberRole(member.get(), roleBan))).when(memberRoleRepository).findByMemberIdAndRoleId(member.get().getId(), roleBan.getId());

        // when
        final Executable executable = () -> memberService.validateAndSaveMember(request);

        // then
        assertThrows(MemberAlreadyBanException.class, executable);
    }

    @Test
    @DisplayName("회원 조회 & JWT 생성: 성공")
    void findMemberAndGenerateJwt_success() throws Exception {
        // given
        doReturn("token").when(kakaoUtil).getAccessTokenFromKakaoAPI(any(String.class));
        doReturn("123456789L").when(kakaoUtil).getUserIdFromKakaoAPI(any(String.class));
        final Optional<Member> member = Optional.of(new Member("123456789L", "12161111@inha.edu", "홍길동", "컴퓨터공학과", MemberGrade.SENIOR, "010-1234-1234", MemberAcademicStatus.ATTENDING, Gender.MAN));
        doReturn(member).when(memberRepository).findByUserId(any(String.class));

        final Role roleResign = new Role(MemberRoles.ROLE_RESIGN.name());
        ReflectionTestUtils.setField(roleResign, "id", 1L);
        doReturn(Optional.of(roleResign)).when(roleRepository).findByName(MemberRoles.ROLE_RESIGN.name());
        final Role roleBan = new Role(MemberRoles.ROLE_BAN.name());
        ReflectionTestUtils.setField(roleBan, "id", 2L);
        doReturn(Optional.of(roleBan)).when(roleRepository).findByName(MemberRoles.ROLE_BAN.name());
        doReturn(Optional.empty()).when(memberRoleRepository).findByMemberIdAndRoleId(any(Long.class), any(Long.class));

        List<GrantedAuthority> authorities = new ArrayList<>();
        final User user = new User(String.valueOf(member.get().getId()), UUID.randomUUID().toString(), authorities);
        doReturn(user).when(jwtUserDetailsUtil).loadUserByUsername(any(String.class));

        ReflectionTestUtils.setField(member.get(), "id", 1L);
        doReturn("accessToken").when(jwtTokenUtil).generateAccessToken(any(UserDetails.class));
        doReturn("refreshToken").when(jwtTokenUtil).generateRefreshToken(any(UserDetails.class));
        doNothing().when(redisUtil).set(any(String.class), any(String.class), any(Integer.class));
        doNothing().when(redisUtil).set(any(String.class), any(Long.class), any(Integer.class));

        // when
        final MemberAndJwtDto memberAndJwtDto = memberService.findMemberAndGenerateJwt("123456789L");

        // then
        assertThat(memberAndJwtDto.getMember().getEmail()).isEqualTo(member.get().getEmail());
    }

    @Test
    @DisplayName("회원 조회 & JWT 생성: 실패")
    void findMemberAndGenerateJwt_fail() throws Exception {
        // given
        doReturn(Optional.empty()).when(memberRepository).findByUserId(any(String.class));
        doReturn("token").when(kakaoUtil).getAccessTokenFromKakaoAPI(any(String.class));
        doReturn("123456789L").when(kakaoUtil).getUserIdFromKakaoAPI(any(String.class));

        // when
        final Executable executable = () -> memberService.findMemberAndGenerateJwt("123456789L");

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
    void checkEmailAndSendMail_success() throws Exception {
        // given
        doReturn(Optional.empty()).when(memberRepository).findByEmail(any(String.class));

        // when
        final EmailCheckResponse response = memberService.checkEmailAndSendMail("12161111@inha.edu");

        // then
        assertThat(response.getStatus()).isEqualTo(Status.SUCCESS);
    }

    @Test
    @DisplayName("이메일 검증: 실패")
    void checkEmailAndSendMail_fail() throws Exception {
        // given
        final Optional<Member> member = Optional.of(new Member("123456789L", "12161111@inha.edu", "홍길동", "컴퓨터공학과", MemberGrade.SENIOR, "010-1234-1234", MemberAcademicStatus.ATTENDING, Gender.MAN));
        doReturn(member).when(memberRepository).findByEmail(any(String.class));

        // when
        final EmailCheckResponse response = memberService.checkEmailAndSendMail("12161111@inha.edu");

        // then
        assertThat(response.getStatus()).isEqualTo(Status.FAILURE);
    }

    @Test
    @DisplayName("회원 정보 조회: 성공")
    void getMemberInfo_success() throws Exception {
        // given
        final Optional<Member> me = Optional.of(new Member("123456789L", "12161111@inha.edu", "username", "컴퓨터공학과", MemberGrade.SENIOR, "010-1234-1234", MemberAcademicStatus.ATTENDING, Gender.MAN));
        ReflectionTestUtils.setField(me.get(), "id", 1L);
        final Optional<Member> someone = Optional.of(new Member("123456789L", "12161111@inha.edu", "asdfasdf", "컴퓨터공학과", MemberGrade.SENIOR, "010-1234-1234", MemberAcademicStatus.ATTENDING, Gender.MAN));
        ReflectionTestUtils.setField(someone.get(), "id", 2L);
        doReturn(me).when(memberRepository).findById(1L);
        doReturn(someone).when(memberRepository).findById(2L);

        // when
        final MemberInfoResponse response = memberService.getMemberInfo(1L);
        final MemberInfoResponse response2 = memberService.getMemberInfo(2L);
        final MemberDto myInfo = (MemberDto) response.getInfo();
        final MemberSimpleDto someoneInfo = (MemberSimpleDto) response2.getInfo();

        // then
        assertThat(myInfo.getStudentId()).isEqualTo("12161111");
        assertThat(someoneInfo.getStudentId()).isEqualTo("16");
    }

    @Test
    @DisplayName("회원 정보 조회: 실패")
    void getMemberInfo_fail() throws Exception {
        // given
        doReturn(Optional.empty()).when(memberRepository).findById(any(Long.class));

        // when
        final Executable executable = () -> memberService.getMemberInfo(1L);

        // then
        assertThrows(MemberNotFoundException.class, executable);
    }

    @Test
    @DisplayName("회원 정보 수정: 성공")
    void updateMemberInfo_success() throws Exception {
        // given
        final Optional<Member> member = Optional.of(new Member("123456789L", "12161111@inha.edu", "홍길동", "컴퓨터공학과", MemberGrade.SENIOR, "010-1234-1234", MemberAcademicStatus.ATTENDING, Gender.MAN));
        ReflectionTestUtils.setField(member.get(), "id", 1L);
        final MemberInfoUpdateRequest request = new MemberInfoUpdateRequest("만두", "정통", "010-3333-1234", MemberAcademicStatus.ATTENDING, MemberGrade.SENIOR, "안녕");
        doReturn(member).when(memberRepository).findById(any(Long.class));

        // when
        final MemberInfoUpdateResponse response = memberService.updateMemberInfo(request);

        // then
        assertThat(response.getStatus()).isEqualTo(Status.SUCCESS);
        assertThat(response.getName()).isEqualTo("만두");
        assertThat(response.getDepartment()).isEqualTo("정통");
        assertThat(response.getPhone()).isEqualTo("010-3333-1234");
        assertThat(response.getIntroduce()).isEqualTo("안녕");
    }

    @Test
    @DisplayName("회원 이미지 변경: 성공(새 이미지)")
    void updateMemberImage_success() throws Exception {
        // given
        final Optional<Member> member = Optional.of(new Member("123456789L", "12161111@inha.edu", "홍길동", "컴퓨터공학과", MemberGrade.SENIOR, "010-1234-1234", MemberAcademicStatus.ATTENDING, Gender.MAN));
        member.get().updateImage(Image.builder().url("다른 이미지 url").build());
        doReturn(member).when(memberRepository).findById(any(Long.class));

        ReflectionTestUtils.setField(imageUtil, "MEMBER_BASIC_IMAGE_URL", "구 이미지 저장소 url");
        doNothing().when(amazonS3Util).deleteImage(any(Image.class), any(String.class));
        final Image image = Image.builder().url("새 이미지 저장소 url").build();
        doReturn(image).when(amazonS3Util).uploadImage(any(MultipartFile.class), any(String.class));

        final MockMultipartFile multipartFile = new MockMultipartFile("name", "name.png", "png", new FileInputStream("src/test/resources/static/image.jpg"));

        // when
        final MemberImageUpdateResponse response = memberService.updateMemberImage(multipartFile);

        // then
        assertThat(response.getStatus()).isEqualTo(Status.SUCCESS);
        assertThat(response.getImageUrl()).isEqualTo("새 이미지 저장소 url");
    }

    @Test
    @DisplayName("회원 이미지 변경: 성공(기본 이미지)")
    void updateMemberImage_success2() throws Exception {
        // given
        final Optional<Member> member = Optional.of(new Member("123456789L", "12161111@inha.edu", "홍길동", "컴퓨터공학과", MemberGrade.SENIOR, "010-1234-1234", MemberAcademicStatus.ATTENDING, Gender.MAN));
        member.get().updateImage(Image.builder().url("다른 이미지 url").build());
        doReturn(member).when(memberRepository).findById(any(Long.class));

        ReflectionTestUtils.setField(imageUtil, "MEMBER_BASIC_IMAGE_URL", "구 이미지 저장소 url");
        doNothing().when(amazonS3Util).deleteImage(any(Image.class), any(String.class));

        // when
        final MemberImageUpdateResponse response = memberService.updateMemberImage(null);

        // then
        assertThat(response.getStatus()).isEqualTo(Status.SUCCESS);
        assertThat(response.getImageUrl()).isEqualTo("구 이미지 저장소 url");
    }

    @Test
    @DisplayName("회원 이미지 변경: 실패")
    void updateMemberImage_fail() throws Exception {
        // given
        final Optional<Member> member = Optional.of(new Member("123456789L", "12161111@inha.edu", "홍길동", "컴퓨터공학과", MemberGrade.SENIOR, "010-1234-1234", MemberAcademicStatus.ATTENDING, Gender.MAN));
        member.get().updateImage(Image.builder().url("구 이미지 저장소 url").build());
        doReturn(member).when(memberRepository).findById(any(Long.class));

        ReflectionTestUtils.setField(imageUtil, "MEMBER_BASIC_IMAGE_URL", "구 이미지 저장소 url");

        // when
        final Executable executable = () -> memberService.updateMemberImage(null);

        // then
        assertThrows(MemberImageAlreadyBasicException.class, executable);
    }

    @Test
    @DisplayName("이메일 인증 여부 확인: 성공")
    void validateEmail_success() throws Exception {
        // given
        doReturn("true").when(redisUtil).get(any(String.class));
        doReturn(true).when(redisUtil).delete(any(String.class));

        // when
        final ValidateEmailResponse response = memberService.validateEmail("11111111@inha.edu");

        // then
        assertThat(response.getStatus()).isEqualTo(Status.SUCCESS);
        assertThat(response.getReason()).isEqualTo(EMAIL_IS_VERIFIED.getMessage());
    }

    @Test
    @DisplayName("이메일 인증 여부 확인: 실패")
    void validateEmail_fail() throws Exception {
        // given
        doReturn(null).when(redisUtil).get(any(String.class));

        // when
        final ValidateEmailResponse response = memberService.validateEmail("11111111@inha.edu");

        // then
        assertThat(response.getStatus()).isEqualTo(Status.FAILURE);
        assertThat(response.getReason()).isEqualTo(EMAIL_NOT_VERIFIED.getMessage());
    }

    @Test
    @DisplayName("권한 요청: 성공")
    void requestAuthority_success() throws Exception {
        // given
        final MemberRoles memberRoles = MemberRoles.ROLE_MEMBER;
        final Optional<Role> role = Optional.of(new Role(memberRoles.name()));
        ReflectionTestUtils.setField(role.get(), "id", 1L);
        doReturn(role).when(roleRepository).findByName(any(String.class));

        final Optional<Member> member = Optional.of(new Member("123456789L", "12161111@inha.edu", "홍길동", "컴퓨터공학과", MemberGrade.SENIOR, "010-1234-1234", MemberAcademicStatus.ATTENDING, Gender.MAN));
        doReturn(member).when(memberRepository).findById(any(Long.class));

        doReturn(Optional.empty()).when(memberRoleRepository).findByMemberIdAndRoleId(any(Long.class), any(Long.class));
        doReturn(null).when(memberRoleRepository).save(any(MemberRole.class));

        // when
        final RequestAuthorityResponse response = memberService.requestAuthority(memberRoles);

        // then
        assertThat(response.getStatus()).isEqualTo(Status.SUCCESS);
        assertThat(response.getRole()).isEqualTo(memberRoles);
    }

    @Test
    @DisplayName("권한 요청: 실패")
    void requestAuthority_fail() throws Exception {
        // given
        final MemberRoles memberRoles = MemberRoles.ROLE_MEMBER;
        final Optional<Role> role = Optional.of(new Role(memberRoles.name()));
        ReflectionTestUtils.setField(role.get(), "id", 1L);
        doReturn(role).when(roleRepository).findByName(any(String.class));

        final Optional<Member> member = Optional.of(new Member("123456789L", "12161111@inha.edu", "홍길동", "컴퓨터공학과", MemberGrade.SENIOR, "010-1234-1234", MemberAcademicStatus.ATTENDING, Gender.MAN));
        doReturn(member).when(memberRepository).findById(any(Long.class));

        final MemberRole memberRole = new MemberRole(member.get(), role.get());
        doReturn(Optional.of(memberRole)).when(memberRoleRepository).findByMemberIdAndRoleId(any(Long.class), any(Long.class));

        // when
        final Executable executable = () -> memberService.requestAuthority(memberRoles);

        // then
        assertThrows(MemberAlreadyHasRoleException.class, executable);
    }

    @Test
    @DisplayName("회원 탈퇴: 성공")
    void resign_success() throws Exception {
        // given
        final Optional<Member> member = Optional.of(new Member("123456789L", "12161111@inha.edu", "홍길동", "컴퓨터공학과", MemberGrade.SENIOR, "010-1234-1234", MemberAcademicStatus.ATTENDING, Gender.MAN));
        doReturn(member).when(memberRepository).findById(any(Long.class));

        final MemberRoles memberRoles = MemberRoles.ROLE_RESIGN;
        final Optional<Role> role = Optional.of(new Role(memberRoles.name()));
        ReflectionTestUtils.setField(role.get(), "id", 1L);
        doReturn(role).when(roleRepository).findByName(any(String.class));

        final MemberRole memberRole = new MemberRole(member.get(), role.get());
        ReflectionTestUtils.setField(memberRole, "id", 1L);
        List<MemberRole> memberRoleList = new ArrayList<>();
        memberRoleList.add(memberRole);
        doReturn(memberRoleList).when(memberRoleRepository).findAllByMemberId(any(Long.class));

        doNothing().when(memberRoleRepository).deleteAllByIdInBatch(any(Iterable.class));
        doReturn(null).when(memberRoleRepository).save(any(MemberRole.class));

        final String code = "123123";
        doReturn(code).when(redisUtil).get(any(String.class));
        doReturn(true).when(redisUtil).delete(any(String.class));

        // when
        final StatusResponse response = memberService.resign(code);

        // then
        assertThat(response.getStatus()).isEqualTo(Status.SUCCESS);
        assertThat(member.get().getEmail()).isEqualTo("");
    }

    @Test
    @DisplayName("회원 탈퇴: 탈퇴할 수 없는 회원")
    void resign_fail() throws Exception {
        // given
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_CLUB_PRESIDENT"));
        User user = new User("1", "password", authorities);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // when
        final Executable executable = () -> memberService.resign("123123");

        // then
        assertThrows(MemberResignException.class, executable);
    }

    @Test
    @DisplayName("회원 탈퇴: 인증코드 불일치")
    void resign_fail2() throws Exception {
        // given
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_CLUB_GUEST"));
        User user = new User("1", "password", authorities);
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        final Optional<Member> member = Optional.of(new Member("123456789L", "12161111@inha.edu", "홍길동", "컴퓨터공학과", MemberGrade.SENIOR, "010-1234-1234", MemberAcademicStatus.ATTENDING, Gender.MAN));
        doReturn(member).when(memberRepository).findById(any(Long.class));

        final String code = "123123";
        final Object invalidCode = "321321";
        doReturn(invalidCode).when(redisUtil).get(any(String.class));

        // when
        final Executable executable = () -> memberService.resign(code);

        // then
        assertThrows(CertificationCodeInvalidException.class, executable);
    }
}
