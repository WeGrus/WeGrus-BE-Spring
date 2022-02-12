package wegrus.clubwebsite.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wegrus.clubwebsite.dto.Status;
import wegrus.clubwebsite.dto.StatusResponse;
import wegrus.clubwebsite.dto.error.ErrorResponse;
import wegrus.clubwebsite.dto.member.*;
import wegrus.clubwebsite.entity.Request;
import wegrus.clubwebsite.entity.member.*;
import wegrus.clubwebsite.exception.*;
import wegrus.clubwebsite.repository.*;

import java.util.*;
import java.util.stream.Collectors;

import static wegrus.clubwebsite.dto.error.ErrorCode.*;
import static wegrus.clubwebsite.entity.member.MemberRoles.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ClubService {

    private final MemberRepository memberRepository;
    private final RequestRepository requestRepository;
    private final MemberRoleRepository memberRoleRepository;
    private final RoleRepository roleRepository;
    private final GroupMemberRepository groupMemberRepository;

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
        }
    }

    private void saveMemberRole(Request request, Role role, Member applicant, Set<String> roles) {
        if (roles.contains(request.getRole().getName())) {
            requestRepository.deleteByMemberIdAndRoleId(applicant.getId(), role.getId());
            memberRoleRepository.save(new MemberRole(applicant, role));
        } else {
            List<ErrorResponse.FieldError> errors = new ArrayList<>();
            errors.add(new ErrorResponse.FieldError("role", request.getRole().getName(), CANNOT_REQUEST_AUTHORITY.getMessage()));
            throw new CannotRequestAuthorityException(errors);
        }
    }

    public Page<RequestDto> getRequestDtoPage(int page, int size, MemberRoles role) {
        final Set<MemberRoles> roles = Set.of(ROLE_MEMBER, ROLE_GROUP_PRESIDENT, ROLE_CLUB_EXECUTIVE);
        if (!roles.contains(role)) {
            List<ErrorResponse.FieldError> errors = new ArrayList<>();
            errors.add(new ErrorResponse.FieldError("role", role.name(), CANNOT_REQUEST_AUTHORITY.getMessage()));
            throw new CannotRequestAuthorityException(errors);
        }

        page = (page == 0 ? 0 : page - 1);
        Pageable pageable = PageRequest.of(page, size);
        return requestRepository.findRequestDtoPageByRole(role, pageable);
    }

    public Page<MemberDto> getMemberDtoPage(int page, int size, MemberSortType type, Sort.Direction direction) {
        page = (page == 0 ? 0 : page - 1);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, type.getField()));
        return memberRepository.findMemberDtoPage(pageable);
    }

    public Page<MemberDto> searchMember(int page, int size, MemberSortType sortType, Sort.Direction direction, MemberSearchType searchType, String word) {
        page = (page == 0 ? 0 : page - 1);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortType.getField()));
        return memberRepository.findMemberDtoPageByWordContainingAtSearchType(pageable, searchType, word);
    }

    public Page<MemberDto> searchMemberByGender(int page, int size, MemberSortType sortType, Sort.Direction direction, Gender gender) {
        page = (page == 0 ? 0 : page - 1);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortType.getField()));
        return memberRepository.findMemberDtoPageByGender(pageable, gender);
    }

    public Page<MemberDto> searchMemberByAcademicStatus(int page, int size, MemberSortType sortType, Sort.Direction direction, MemberAcademicStatus academicStatus) {
        page = (page == 0 ? 0 : page - 1);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortType.getField()));
        return memberRepository.findMemberDtoPageByAcademicStatus(pageable, academicStatus);
    }

    public Page<MemberDto> searchMemberByGrade(int page, int size, MemberSortType sortType, Sort.Direction direction, MemberGrade grade) {
        page = (page == 0 ? 0 : page - 1);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortType.getField()));
        return memberRepository.findMemberDtoPageByGrade(pageable, grade);
    }

    public Page<MemberDto> searchMembersByAuthority(int page, int size, MemberSortType sortType, Sort.Direction direction, MemberRoleSearchType authority) {
        page = (page == 0 ? 0 : page - 1);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortType.getField()));
        return memberRepository.findMemberDtoPageByAuthority(pageable, authority);
    }

    public Page<MemberDto> searchMembersByGroup(int page, int size, MemberSortType sortType, Sort.Direction direction, Long groupId) {
        page = (page == 0 ? 0 : page - 1);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortType.getField()));
        return memberRepository.findMemberDtoPageByGroup(pageable, groupId);
    }

    @Transactional
    public StatusResponse deleteAuthority(Long memberId, MemberRoleDeleteType type) {
        final Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        final Role role = roleRepository.findByName(type.name()).orElseThrow(MemberRoleNotFoundException::new);
        final Optional<MemberRole> memberRole = memberRoleRepository.findByMemberIdAndRoleId(member.getId(), role.getId());

        if (memberRole.isEmpty())
            throw new MemberRoleNotFoundException();
        memberRoleRepository.delete(memberRole.get());

        return new StatusResponse(Status.SUCCESS);
    }

    @Transactional
    public StatusResponse banMember(Long memberId) {
        final Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        final List<MemberRole> memberRoles = memberRoleRepository.findAllWithRoleByMemberId(member.getId());
        validateMemberRole(memberRoles);

        final Role role = roleRepository.findByName(ROLE_BAN.name()).orElseThrow(MemberRoleNotFoundException::new);
        final MemberRole memberRole = new MemberRole(member, role);

        groupMemberRepository.deleteAllByMemberId(member.getId());
        memberRoleRepository.deleteAllByMemberId(member.getId());
        memberRoleRepository.save(memberRole);
        member.resign();

        return new StatusResponse(Status.SUCCESS);
    }

    private void validateMemberRole(List<MemberRole> memberRoles) {
        final List<ErrorResponse.FieldError> errors = new ArrayList<>();
        memberRoles.forEach(mr -> {
            if (mr.getRole().getName().equals(ROLE_BAN.name()))
                errors.add(new ErrorResponse.FieldError("role", ROLE_BAN.name(), MEMBER_ALREADY_BAN.getMessage()));
            else if (mr.getRole().getName().equals(ROLE_RESIGN.name()))
                errors.add(new ErrorResponse.FieldError("role", ROLE_RESIGN.name(), MEMBER_ALREADY_RESIGN.getMessage()));
            else if (mr.getRole().getName().equals(ROLE_CLUB_PRESIDENT.name()))
                errors.add(new ErrorResponse.FieldError("role", ROLE_CLUB_PRESIDENT.name(), CLUB_PRESIDENT_CANNOT_RESIGN.getMessage()));
        });
        if (!errors.isEmpty())
            throw new CannotBanMember(errors);
    }
}
