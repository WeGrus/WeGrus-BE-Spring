package wegrus.clubwebsite.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wegrus.clubwebsite.dto.Status;
import wegrus.clubwebsite.dto.member.EmailCheckResponse;
import wegrus.clubwebsite.dto.member.JwtDto;
import wegrus.clubwebsite.util.JwtTokenUtil;
import wegrus.clubwebsite.dto.VerificationResponse;
import wegrus.clubwebsite.dto.error.ErrorResponse;
import wegrus.clubwebsite.dto.member.MemberAndJwtDto;
import wegrus.clubwebsite.dto.member.MemberSignupRequest;
import wegrus.clubwebsite.entity.Member;
import wegrus.clubwebsite.entity.MemberRole;
import wegrus.clubwebsite.exception.MemberAlreadyExistException;
import wegrus.clubwebsite.exception.MemberNotFoundException;
import wegrus.clubwebsite.repository.MemberRepository;
import wegrus.clubwebsite.util.JwtUserDetailsUtil;
import wegrus.clubwebsite.util.RedisUtil;

import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

import static wegrus.clubwebsite.dto.error.ErrorCode.*;
import static wegrus.clubwebsite.dto.result.ResultCode.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MailService mailService;
    private final RedisUtil redisUtil;
    private final JwtTokenUtil jwtTokenUtil;
    private final JwtUserDetailsUtil jwtUserDetailsUtil;

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
        final Member member = memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);
        member.updateRole(MemberRole.ROLE_CERTIFIED);
        // TODO: 회원가입 축하 이메일 전송 -> 내용 논의

        return new VerificationResponse(true, VERIFY_EMAIL_SUCCESS.getMessage());
    }

    @Transactional
    public String validateAndSendVerificationMailAndSaveMember(MemberSignupRequest request) throws MessagingException {
        validateDuplication(request.getEmail(), request.getKakaoId());
        final Member member = Member.builder()
                .kakaoId(request.getKakaoId())
                .email(request.getEmail())
                .academicStatus(request.getAcademicStatus())
                .department(request.getDepartment())
                .grade(request.getGrade())
                .name(request.getName())
                .phone(request.getPhone())
                .studentId(request.getEmail().substring(0, 8))
                .build();

        memberRepository.save(member);
        return sendVerificationMail(request.getEmail());
    }

    private void validateDuplication(String email, Long kakaoId) {
        final Optional<Member> findMember = memberRepository.findByKakaoIdOrEmail(kakaoId, email);
        if (findMember.isPresent()) {
            List<ErrorResponse.FieldError> errors = new ArrayList<>();
            if (findMember.get().getEmail().equals(email))
                errors.add(new ErrorResponse.FieldError("email", email, EMAIL_ALREADY_EXIST.getMessage()));
            if (findMember.get().getKakaoId().equals(kakaoId))
                errors.add(new ErrorResponse.FieldError("kakaoId", String.valueOf(kakaoId), KAKAOID_ALREADY_EXIST.getMessage()));

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

    public MemberAndJwtDto findMemberAndGenerateJwt(Long kakaoId) {
        final Member member = memberRepository.findByKakaoId(kakaoId).orElseThrow(MemberNotFoundException::new);
        UserDetails userDetails = jwtUserDetailsUtil.loadUserByUsername(String.valueOf(member.getId()));
        final String accessToken = jwtTokenUtil.generateAccessToken(userDetails);
        final String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails);

        redisUtil.set(refreshToken, member.getId(), REFRESH_TOKEN_VALID_TIME);
        return new MemberAndJwtDto(member, accessToken, refreshToken);
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

    public EmailCheckResponse checkEmail(String email) {
        if (memberRepository.findByEmail(email).isPresent())
            return new EmailCheckResponse(Status.FAILURE, EMAIL_ALREADY_EXIST.getMessage());
        else if (!Pattern.matches("^[0-9]{8}@(inha.edu|inha.ac.kr)$", email))
            return new EmailCheckResponse(Status.FAILURE, INVALID_EMAIL.getMessage());
        return new EmailCheckResponse(Status.SUCCESS, VALID_EMAIL.getMessage());
    }
}
