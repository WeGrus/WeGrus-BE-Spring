package wegrus.clubwebsite.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
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
    public StatusResponse approveRequest(Long requestId) {
        final Request request = requestRepository.findWithRoleById(requestId).orElseThrow(RequestNotFoundException::new);
        final Role role = request.getRole();

        final Member applicant = memberRepository.findById(request.getMember().getId()).orElseThrow(MemberNotFoundException::new);
        if (memberRoleRepository.findByMemberIdAndRoleId(applicant.getId(), role.getId()).isPresent())
            throw new MemberAlreadyHasRoleException();

        final Set<String> roles = Set.of(ROLE_MEMBER.name());
        saveMemberRole(request, role, applicant, roles);
        return new StatusResponse(Status.SUCCESS);
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

    public Page<RequestDto> getRequestDtoPage(int page, int size) {
        page = (page == 0 ? 0 : page - 1);
        Pageable pageable = PageRequest.of(page, size);
        return requestRepository.findRequestDtoPageByRole(ROLE_MEMBER, pageable);
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

    @Transactional
    public StatusResponse delegatePresident(Long memberId) {
        final Role roleMember = roleRepository.findByName(ROLE_MEMBER.name()).orElseThrow(MemberRoleNotFoundException::new);
        final Member nextPresident = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        if (memberRoleRepository.findByMemberIdAndRoleId(nextPresident.getId(), roleMember.getId()).isEmpty())
            throw new CannotDelegateMember();

        final Role roleClubPresident = roleRepository.findByName(ROLE_CLUB_PRESIDENT.name()).orElseThrow(MemberRoleNotFoundException::new);
        final Long presidentId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        memberRoleRepository.deleteByMemberIdAndRoleId(presidentId, roleClubPresident.getId());
        memberRoleRepository.save(new MemberRole(nextPresident, roleClubPresident));

        return new StatusResponse(Status.SUCCESS);
    }

    @Transactional
    public StatusResponse resetAuthorities() {
        final List<Long> roleIds = new ArrayList<>();
        final List<Role> roles = roleRepository.findAll();
        roles.forEach(r -> {
            if (r.getName().equals(ROLE_CLUB_EXECUTIVE.name()) || r.getName().equals(ROLE_GROUP_EXECUTIVE.name()) ||
                    r.getName().equals(ROLE_GROUP_PRESIDENT.name()) || r.getName().equals(ROLE_MEMBER.name()))
                roleIds.add(r.getId());
        });


        final List<MemberRole> memberRoles = memberRoleRepository.findAllByRoleIdIn(roleIds);
        memberRoleRepository.deleteAllInBatch(memberRoles);

        final Long memberId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        final Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        Role role = null;
        for (Role r : roles) {
            if (r.getName().equals(ROLE_MEMBER.name()))
                role = r;
        }
        memberRoleRepository.save(new MemberRole(member, role));
        return new StatusResponse(Status.SUCCESS);
    }

    @Transactional
    public StatusResponse rejectRequest(Long requestId) {
        final Request request = requestRepository.findById(requestId).orElseThrow(RequestNotFoundException::new);
        requestRepository.delete(request);

        return new StatusResponse(Status.SUCCESS);
    }

    @Transactional
    public StatusResponse empower(Long memberId, MemberEmpowerType type) {
        final Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        final Role role = roleRepository.findByName(type.name()).orElseThrow(MemberRoleNotFoundException::new);
        if (memberRoleRepository.findByMemberIdAndRoleId(member.getId(), role.getId()).isPresent())
            throw new MemberAlreadyHasRoleException();

        memberRoleRepository.save(new MemberRole(member, role));

        return new StatusResponse(Status.SUCCESS);
    }
}
