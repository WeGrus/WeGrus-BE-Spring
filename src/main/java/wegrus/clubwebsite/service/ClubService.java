package wegrus.clubwebsite.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wegrus.clubwebsite.dto.Status;
import wegrus.clubwebsite.dto.StatusResponse;
import wegrus.clubwebsite.dto.error.ErrorCode;
import wegrus.clubwebsite.dto.error.ErrorResponse;
import wegrus.clubwebsite.entity.Request;
import wegrus.clubwebsite.entity.member.Member;
import wegrus.clubwebsite.entity.member.MemberRole;
import wegrus.clubwebsite.entity.member.Role;
import wegrus.clubwebsite.exception.*;
import wegrus.clubwebsite.repository.MemberRepository;
import wegrus.clubwebsite.repository.MemberRoleRepository;
import wegrus.clubwebsite.repository.RequestRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static wegrus.clubwebsite.entity.member.MemberRoles.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ClubService {

    private final MemberRepository memberRepository;
    private final RequestRepository requestRepository;
    private final MemberRoleRepository memberRoleRepository;

    @Transactional
    public StatusResponse empower(Long requestId) {
        final Request request = requestRepository.findWithRoleById(requestId).orElseThrow(RequestNotFoundException::new);
        final Role role = request.getRole();
        final Set<String> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        final Member applicant = memberRepository.findById(request.getMember().getId()).orElseThrow(MemberNotFoundException::new);
        if (memberRoleRepository.findByMemberIdAndRoleId(applicant.getId(), role.getId()).isPresent())
            throw new MemberAlreadyHasRoleException();

        addAuthority(request, authorities, role, applicant);
        return new StatusResponse(Status.SUCCESS);
    }

    private void addAuthority(Request request, Set<String> authorities, Role role, Member applicant) {
        if (authorities.contains(ROLE_CLUB_PRESIDENT.name())) {
            final Set<String> roles = Set.of(ROLE_MEMBER.name(), ROLE_GROUP_PRESIDENT.name(), ROLE_CLUB_EXECUTIVE.name());
            saveMemberRole(request, role, applicant, roles);
        } else if (authorities.contains(ROLE_CLUB_EXECUTIVE.name())) {
            final Set<String> roles = Set.of(ROLE_MEMBER.name(), ROLE_GROUP_PRESIDENT.name());
            if (request.getRole().getName().equals(ROLE_CLUB_EXECUTIVE.name())) {
                List<ErrorResponse.FieldError> errors = new ArrayList<>();
                errors.add(new ErrorResponse.FieldError("authority", ROLE_CLUB_PRESIDENT.name(), "해당 권한이 부족합니다."));
                throw new InsufficientAuthorityException(errors);
            }
            saveMemberRole(request, role, applicant, roles);
        } else {
            List<ErrorResponse.FieldError> errors = new ArrayList<>();
            if (request.getRole().getName().equals(ROLE_CLUB_EXECUTIVE.name())) {
                errors.add(new ErrorResponse.FieldError("authority", ROLE_CLUB_PRESIDENT.name(), "해당 권한이 부족합니다."));
            } else {
                errors.add(new ErrorResponse.FieldError("authority", ROLE_CLUB_EXECUTIVE.name(), "해당 권한이 부족합니다."));
            }
            throw new InsufficientAuthorityException(errors);
        }
    }

    private void saveMemberRole(Request request, Role role, Member applicant, Set<String> roles) {
        if (roles.contains(request.getRole().getName())) {
            requestRepository.deleteByMemberIdAndRoleId(applicant.getId(), role.getId());
            memberRoleRepository.save(new MemberRole(applicant, role));
        } else {
            List<ErrorResponse.FieldError> errors = new ArrayList<>();
            errors.add(new ErrorResponse.FieldError("role", request.getRole().getName(), ErrorCode.CANNOT_REQUEST_AUTHORITY.getMessage()));
            throw new CannotRequestAuthorityException(errors);
        }
    }
}
