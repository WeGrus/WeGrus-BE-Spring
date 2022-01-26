package wegrus.clubwebsite.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
import wegrus.clubwebsite.entity.member.MemberRole;
import wegrus.clubwebsite.entity.member.MemberRoles;
import wegrus.clubwebsite.exception.*;
import wegrus.clubwebsite.repository.MemberRoleRepository;
import wegrus.clubwebsite.repository.RoleRepository;
import wegrus.clubwebsite.util.*;
import wegrus.clubwebsite.dto.VerificationResponse;
import wegrus.clubwebsite.dto.error.ErrorResponse;
import wegrus.clubwebsite.entity.member.Member;
import wegrus.clubwebsite.entity.member.Role;
import wegrus.clubwebsite.repository.MemberRepository;
import wegrus.clubwebsite.vo.Image;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static wegrus.clubwebsite.dto.error.ErrorCode.*;
import static wegrus.clubwebsite.dto.result.ResultCode.*;
import static wegrus.clubwebsite.util.ImageUtil.MEMBER_BASIC_IMAGE_URL;

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

    @Value("${valid-time.verification-key}")
    private Integer VERIFICATION_KEY_VALID_TIME;
    @Value("${valid-time.refresh-token}")
    private Integer REFRESH_TOKEN_VALID_TIME;

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
                .grade(request.getGrade())
                .name(request.getName())
                .phone(request.getPhone())
                .build();
        memberRepository.save(member);
        amazonS3Util.createDirectory("members/" + member.getId());

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
            }
            else if (memberRoleRepository.findByMemberIdAndRoleId(memberId, banId).isPresent())
                throw new MemberAlreadyBanException();
        }
        return null;
    }

    private void validateDuplication(String email, String userId) {
        final Optional<Member> findMember = memberRepository.findByUserIdOrEmail(userId, email);
        if (findMember.isPresent()) {
            List<ErrorResponse.FieldError> errors = new ArrayList<>();
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

    public MemberAndJwtDto findMemberAndGenerateJwt(String authorizationCode) {
        final String userId = kakaoUtil.getUserIdFromKakaoAPI(kakaoUtil.getAccessTokenFromKakaoAPI(authorizationCode));

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

        redisUtil.set(refreshToken, member.getId(), REFRESH_TOKEN_VALID_TIME);
        return new MemberAndJwtDto(new MemberDto(member), accessToken, refreshToken);
    }

    public void deleteRefreshToken(String refreshToken) {
        redisUtil.delete(refreshToken);
    }

    public JwtDto reIssueJwt(String refreshToken) {
        jwtTokenUtil.validateRefreshToken(refreshToken);
        redisUtil.delete(refreshToken);

        final UserDetails userDetails = jwtUserDetailsUtil.loadUserByUsername(jwtTokenUtil.getUsernameFromRefreshToken(refreshToken));
        final String newAccessToken = jwtTokenUtil.generateAccessToken(userDetails);
        final String newRefreshToken = jwtTokenUtil.generateRefreshToken(userDetails);

        redisUtil.set(newRefreshToken, userDetails.getUsername(), REFRESH_TOKEN_VALID_TIME);
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
        if (member.getId().toString().equals(SecurityContextHolder.getContext().getAuthentication().getName()))
            return new MemberInfoResponse(Status.SUCCESS, new MemberDto(member));
        else
            return new MemberInfoResponse(Status.SUCCESS, new MemberSimpleDto(member));
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
    public RequestAuthorityResponse requestAuthority(MemberRoles memberRoles) {
        final Role role = roleRepository.findByName(memberRoles.name()).orElseThrow(MemberRoleNotFoundException::new);
        final Long memberId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        final Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);

        if (memberRoleRepository.findByMemberIdAndRoleId(memberId, role.getId()).isPresent())
            throw new MemberAlreadyHasRoleException();
        memberRoleRepository.save(new MemberRole(member, role));
        return new RequestAuthorityResponse(Status.SUCCESS, memberRoles);
    }

    public StatusResponse resign() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        validateMemberRole(authentication);

        final Long memberId = Long.valueOf(authentication.getName());
        final Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        final Role role = roleRepository.findByName(MemberRoles.ROLE_RESIGN.name()).orElseThrow(MemberRoleNotFoundException::new);
        final MemberRole memberRole = new MemberRole(member, role);

        final List<Long> ids = memberRoleRepository.findAllByMemberId(memberId).stream()
                .map(MemberRole::getId)
                .collect(Collectors.toList());
        memberRoleRepository.deleteAllByIdInBatch(ids);
        memberRoleRepository.save(memberRole);
        member.resign();

        return new StatusResponse(Status.SUCCESS);
    }

    private void validateMemberRole(Authentication authentication) {
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
        if (!errors.isEmpty())
            throw new MemberResignException(errors);
    }
}
