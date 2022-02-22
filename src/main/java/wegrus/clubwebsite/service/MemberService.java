package wegrus.clubwebsite.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import wegrus.clubwebsite.dto.Status;
import wegrus.clubwebsite.dto.StatusResponse;
import wegrus.clubwebsite.dto.member.*;
import wegrus.clubwebsite.dto.post.BookmarkDto;
import wegrus.clubwebsite.dto.post.PostDto;
import wegrus.clubwebsite.dto.post.PostReplyDto;
import wegrus.clubwebsite.entity.Request;
import wegrus.clubwebsite.entity.group.Group;
import wegrus.clubwebsite.entity.group.GroupMember;
import wegrus.clubwebsite.entity.group.GroupRoles;
import wegrus.clubwebsite.entity.member.MemberRole;
import wegrus.clubwebsite.entity.member.MemberRoles;
import wegrus.clubwebsite.exception.*;
import wegrus.clubwebsite.repository.*;
import wegrus.clubwebsite.util.*;
import wegrus.clubwebsite.dto.VerificationResponse;
import wegrus.clubwebsite.dto.error.ErrorResponse;
import wegrus.clubwebsite.entity.member.Member;
import wegrus.clubwebsite.entity.member.Role;
import wegrus.clubwebsite.vo.Image;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import static wegrus.clubwebsite.dto.error.ErrorCode.*;
import static wegrus.clubwebsite.dto.result.ResultCode.*;
import static wegrus.clubwebsite.entity.group.GroupRoles.*;
import static wegrus.clubwebsite.entity.member.MemberRoles.*;
import static wegrus.clubwebsite.util.ImageUtil.MEMBER_BASIC_IMAGE_URL;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberRoleRepository memberRoleRepository;
    private final RoleRepository roleRepository;
    private final MailService mailService;
    private final RedisUtil redisUtil;
    private final JwtTokenUtil jwtTokenUtil;
    private final JwtUserDetailsUtil jwtUserDetailsUtil;
    private final AmazonS3Util amazonS3Util;
    private final KakaoUtil kakaoUtil;
    private final PostRepository postRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final RequestRepository requestRepository;

    @Value("${valid-time.verification-key}")
    private Integer VERIFICATION_KEY_VALID_TIME;

    @Transactional
    public VerificationResponse checkVerificationKey(String verificationKey) {
        final String email = (String) redisUtil.get(verificationKey);

        if (email == null)
            return new VerificationResponse(false, EXPIRED_VERIFICATION_KEY.getMessage());
        redisUtil.delete(verificationKey);
        redisUtil.set(email, email, 30);

        return new VerificationResponse(true, VERIFY_EMAIL_SUCCESS.getMessage());
    }

    @Transactional
    public MemberSignupResponse validateAndSaveMember(MemberSignupRequest request) {
        MemberSignupResponse response = checkIsResignedMember(request);
        if (response != null)
            return response;
        validateDuplication(request.getEmail(), request.getUserId());

        final Member member = Member.builder()
                .userId(request.getUserId())
                .email(request.getEmail())
                .academicStatus(request.getAcademicStatus())
                .department(request.getDepartment())
                .gender(request.getGender())
                .grade(request.getGrade())
                .name(request.getName())
                .phone(request.getPhone())
                .build();
        memberRepository.save(member);
        log.info("member saved : {}", member.toString());
        amazonS3Util.createDirectory("members/" + member.getId());
        log.info("create directory in s3");

        final Role role = roleRepository.findByName(MemberRoles.ROLE_GUEST.name()).get();
        memberRoleRepository.save(new MemberRole(member, role));
        // TODO: 회원가입 축하 이메일 전송 -> 내용 논의

        return new MemberSignupResponse(new MemberDto(member));
    }

    private MemberSignupResponse checkIsResignedMember(MemberSignupRequest request) {
        final Optional<Member> findMember = memberRepository.findByUserId(request.getUserId());
        if (findMember.isPresent()) {
            final Long memberId = findMember.get().getId();
            final Long resignId = roleRepository.findByName(MemberRoles.ROLE_RESIGN.name()).get().getId();
            final Long banId = roleRepository.findByName(MemberRoles.ROLE_BAN.name()).get().getId();

            final Optional<MemberRole> resignRole = memberRoleRepository.findByMemberIdAndRoleId(memberId, resignId);
            if (resignRole.isPresent()) {
                final Member member = findMember.get();
                member.rejoin(request);
                memberRoleRepository.deleteById(resignRole.get().getId());

                final Role role = roleRepository.findByName(MemberRoles.ROLE_GUEST.name()).get();
                memberRoleRepository.save(new MemberRole(member, role));
                // TODO: 회원가입 축하 이메일 전송 -> 내용 논의

                return new MemberSignupResponse(new MemberDto(member));
            } else if (memberRoleRepository.findByMemberIdAndRoleId(memberId, banId).isPresent())
                throw new MemberAlreadyBanException();
        }
        return null;
    }

    private void validateDuplication(String email, String userId) {
        final Optional<Member> findMember = memberRepository.findByUserIdOrEmail(userId, email);
        if (findMember.isPresent()) {
            final List<ErrorResponse.FieldError> errors = new ArrayList<>();
            if (findMember.get().getEmail().equals(email))
                errors.add(new ErrorResponse.FieldError("email", email, EMAIL_ALREADY_EXIST.getMessage()));
            if (findMember.get().getUserId().equals(userId))
                errors.add(new ErrorResponse.FieldError("userId", userId, USERID_ALREADY_EXIST.getMessage()));

            if (!errors.isEmpty())
                throw new MemberAlreadyExistException(INVALID_INPUT_VALUE, errors);
        }
    }

    private String sendVerificationMail(String email) throws MessagingException {
        final String verificationKey = UUID.randomUUID().toString();
        mailService.sendSchoolMailVerification(email, verificationKey);
        redisUtil.set(verificationKey, email, VERIFICATION_KEY_VALID_TIME);

        return verificationKey;
    }

    @Transactional
    public MemberAndJwtDto findMemberAndGenerateJwt(String authorizationCode) {
        log.info("authorizationCode = {}", authorizationCode);
        final String userId = kakaoUtil.getUserIdFromKakaoAPI(kakaoUtil.getAccessTokenFromKakaoAPI(authorizationCode));
        log.info("userId = {}", userId);

        redisUtil.set(authorizationCode, userId, 1);
        final Member member = memberRepository.findByUserId(userId).orElseThrow(MemberNotFoundException::new);
        final Long resignId = roleRepository.findByName(MemberRoles.ROLE_RESIGN.name()).get().getId();
        final Long banId = roleRepository.findByName(MemberRoles.ROLE_BAN.name()).get().getId();
        if (memberRoleRepository.findByMemberIdAndRoleId(member.getId(), resignId).isPresent())
            throw new MemberAlreadyResignException();
        else if (memberRoleRepository.findByMemberIdAndRoleId(member.getId(), banId).isPresent())
            throw new MemberAlreadyBanException();
        redisUtil.delete(authorizationCode);

        UserDetails userDetails = jwtUserDetailsUtil.loadUserByUsername(String.valueOf(member.getId()));
        final String accessToken = jwtTokenUtil.generateAccessToken(userDetails);
        final String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails);

        member.updateRefreshToken(refreshToken);
        final MemberDto memberDto = memberRepository.findMemberDtoById(member.getId()).get();
        return new MemberAndJwtDto(memberDto, accessToken, refreshToken);
    }

    @Transactional
    public JwtDto reIssueJwt(String refreshToken) {
        jwtTokenUtil.validateRefreshToken(refreshToken);
        final Long memberId = Long.valueOf(jwtTokenUtil.getUsernameFromRefreshToken(refreshToken));
        final Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        if (!member.getRefreshToken().equals(refreshToken)) {
            List<ErrorResponse.FieldError> errors = new ArrayList<>();
            errors.add(new ErrorResponse.FieldError("refreshToken", refreshToken, INVALID_JWT.getMessage()));
            throw new JwtInvalidException(errors);
        }

        final UserDetails userDetails = jwtUserDetailsUtil.loadUserByUsername(member.getId().toString());
        final String newAccessToken = jwtTokenUtil.generateAccessToken(userDetails);
        final String newRefreshToken = jwtTokenUtil.generateRefreshToken(userDetails);
        member.updateRefreshToken(newRefreshToken);

        return new JwtDto(newAccessToken, newRefreshToken);
    }

    public EmailCheckResponse checkEmailAndSendMail(String email) throws MessagingException {
        if (memberRepository.findByEmail(email).isPresent())
            return new EmailCheckResponse(Status.FAILURE, EMAIL_ALREADY_EXIST.getMessage());
        else if (!Pattern.matches("^[0-9]{8}@(inha.edu|inha.ac.kr)$", email))
            return new EmailCheckResponse(Status.FAILURE, INVALID_EMAIL.getMessage());
        final String verificationKey = sendVerificationMail(email);
        return new EmailCheckResponse(Status.SUCCESS, VALID_EMAIL.getMessage(), verificationKey);
    }

    public MemberInfoResponse getMemberInfo(Long memberId) {
        final Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        if (member.getId().toString().equals(SecurityContextHolder.getContext().getAuthentication().getName())) {
            final MemberDto memberDto = memberRepository.findMemberDtoById(member.getId()).get();
            return new MemberInfoResponse(Status.SUCCESS, memberDto);
        } else {
            final MemberSimpleDto memberSimpleDto = memberRepository.findMemberSimpleDtoById(member.getId()).get();
            return new MemberInfoResponse(Status.SUCCESS, memberSimpleDto);
        }
    }

    @Transactional
    public MemberInfoUpdateResponse updateMemberInfo(MemberInfoUpdateRequest request) {
        final Long memberId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        final Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        member.update(request);
        return new MemberInfoUpdateResponse(Status.SUCCESS, member);
    }

    @Transactional
    public MemberImageUpdateResponse updateMemberImage(MultipartFile multipartFile) throws IOException {
        final Long memberId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        final Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);

        final String dirName = "members/" + member.getId();
        if (multipartFile == null) {
            if (member.getImage().getUrl().equals(MEMBER_BASIC_IMAGE_URL))
                throw new MemberImageAlreadyBasicException();
            amazonS3Util.deleteImage(member.getImage(), dirName);
            member.updateImage(Image.builder().url(MEMBER_BASIC_IMAGE_URL).build());
        } else {
            if (!member.getImage().getUrl().equals(MEMBER_BASIC_IMAGE_URL))
                amazonS3Util.deleteImage(member.getImage(), dirName);
            final Image image = amazonS3Util.uploadImage(multipartFile, dirName);
            member.updateImage(image);
        }

        return new MemberImageUpdateResponse(Status.SUCCESS, member.getImage().getUrl());
    }

    public ValidateEmailResponse validateEmail(String email) {
        final String result = (String) redisUtil.get(email);
        if (result == null)
            return new ValidateEmailResponse(Status.FAILURE, EMAIL_NOT_VERIFIED.getMessage());
        redisUtil.delete(email);
        return new ValidateEmailResponse(Status.SUCCESS, EMAIL_IS_VERIFIED.getMessage());
    }

    @Transactional
    public RequestAuthorityResponse applyToClub() {
        final Role role = roleRepository.findByName(ROLE_MEMBER.name()).orElseThrow(MemberRoleNotFoundException::new);
        final Long memberId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        final Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);

        if (requestRepository.findByMemberIdAndRoleId(memberId, role.getId()).isPresent())
            return new RequestAuthorityFailResponse(Status.FAILURE, REQUEST_ALREADY_EXIST.getMessage());

        if (memberRoleRepository.findByMemberIdAndRoleId(memberId, role.getId()).isPresent())
            throw new MemberAlreadyHasRoleException();

        final Set<MemberRoles> roles = Set.of(ROLE_MEMBER, ROLE_GROUP_PRESIDENT, ROLE_CLUB_EXECUTIVE);
        if (roles.contains(ROLE_MEMBER)) {
            requestRepository.save(new Request(member, role));
        } else {
            List<ErrorResponse.FieldError> errors = new ArrayList<>();
            errors.add(new ErrorResponse.FieldError("role", ROLE_MEMBER.name(), ALREADY_HAVE_AUTHORITY.getMessage()));
            throw new CannotRequestAuthorityException(errors);
        }

        return new RequestAuthoritySuccessResponse(Status.SUCCESS, ROLE_MEMBER);
    }

    @Transactional
    public StatusResponse resign(String certificationCode) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final Long memberId = Long.valueOf(authentication.getName());
        final List<GroupMember> groupMembers = groupMemberRepository.findAllByMemberId(memberId);
        validateMemberRole(authentication, groupMembers);

        final Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        final String code = (String) redisUtil.get(member.getEmail());
        if (code == null || !code.equals(certificationCode))
            throw new CertificationCodeInvalidException();

        final Role role = roleRepository.findByName(MemberRoles.ROLE_RESIGN.name()).orElseThrow(MemberRoleNotFoundException::new);
        final MemberRole memberRole = new MemberRole(member, role);

        groupMemberRepository.deleteAllByMemberId(member.getId());
        memberRoleRepository.deleteAllByMemberId(member.getId());
        memberRoleRepository.save(memberRole);
        member.resign();
        redisUtil.delete(member.getEmail());

        return new StatusResponse(Status.SUCCESS);
    }

    private void validateMemberRole(Authentication authentication, List<GroupMember> groupMembers) {
        final Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        final List<ErrorResponse.FieldError> errors = new ArrayList<>();
        authorities
                .forEach(o -> {
                    if (o.getAuthority().equals(MemberRoles.ROLE_CLUB_PRESIDENT.name()))
                        errors.add(new ErrorResponse.FieldError("role", o.getAuthority(), CLUB_PRESIDENT_CANNOT_RESIGN.getMessage()));
                    else if (o.getAuthority().equals(MemberRoles.ROLE_RESIGN.name()))
                        errors.add(new ErrorResponse.FieldError("role", o.getAuthority(), MEMBER_ALREADY_RESIGN.getMessage()));
                    else if (o.getAuthority().equals(MemberRoles.ROLE_BAN.name()))
                        errors.add(new ErrorResponse.FieldError("role", o.getAuthority(), MEMBER_ALREADY_BAN.getMessage()));
                });
        groupMembers
                .forEach(gm -> {
                    if (gm.getRole().equals(GroupRoles.PRESIDENT))
                        errors.add(new ErrorResponse.FieldError("groupRole", GroupRoles.PRESIDENT.name(), GROUP_PRESIDENT_CANNOT_RESIGN.getMessage()));
                });
        if (!errors.isEmpty())
            throw new MemberResignException(errors);
    }

    public StatusResponse sendRandomCode() {
        final Long memberId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        final Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        final String code = mailService.sendCertificationCode(member.getEmail());
        redisUtil.set(member.getEmail(), code, 30);

        return new StatusResponse(Status.SUCCESS);
    }

    public Page<PostDto> getMyPosts(int page, int size) {
        final Long memberId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());

        page = (page == 0 ? 0 : page - 1);
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findPostDtoPageByMemberIdOrderByCreatedDateDesc(memberId, pageable);
    }

    public Page<PostReplyDto> getMyReplies(int page, int size) {
        final Long memberId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());

        page = (page == 0 ? 0 : page - 1);
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findPostReplyDtoPageByMemberIdOrderByCreatedDateDesc(memberId, pageable);
    }

    public Page<BookmarkDto> getMyBookmarks(int page, int size) {
        final Long memberId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());

        page = (page == 0 ? 0 : page - 1);
        Pageable pageable = PageRequest.of(page, size);
        return postRepository.findBookmarkedPostDtoPageByMemberIdOrderByCreatedDateDesc(memberId, pageable);
    }

    @Transactional
    public StatusResponse applyToGroup(Long groupId) {
        final Long memberId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        final Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        final Group group = groupRepository.findById(groupId).orElseThrow(GroupNotFoundException::new);
        final Optional<GroupMember> findGroupMember = groupMemberRepository.findByMemberIdAndGroupId(memberId, groupId);
        if (findGroupMember.isPresent()) {
            if (findGroupMember.get().getRole().equals(APPLICANT))
                throw new MemberAlreadyApplyGroupException();
            throw new GroupMemberAlreadyExistException();
        }

        final GroupMember groupMember = new GroupMember(member, group);
        groupMemberRepository.save(groupMember);

        return new StatusResponse(Status.SUCCESS);
    }

    public List<Group> getGroups() {
        return groupRepository.findAll();
    }
}
