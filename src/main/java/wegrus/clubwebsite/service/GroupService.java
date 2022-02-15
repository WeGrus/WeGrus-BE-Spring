package wegrus.clubwebsite.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import wegrus.clubwebsite.dto.Status;
import wegrus.clubwebsite.dto.StatusResponse;
import wegrus.clubwebsite.dto.error.ErrorResponse;
import wegrus.clubwebsite.dto.member.MemberDto;
import wegrus.clubwebsite.dto.member.MemberSearchType;
import wegrus.clubwebsite.dto.member.MemberSortType;
import wegrus.clubwebsite.entity.group.Group;
import wegrus.clubwebsite.entity.group.GroupMember;
import wegrus.clubwebsite.entity.group.GroupRoles;
import wegrus.clubwebsite.entity.member.*;
import wegrus.clubwebsite.exception.*;
import wegrus.clubwebsite.repository.*;

import java.util.ArrayList;
import java.util.List;

import static wegrus.clubwebsite.dto.error.ErrorCode.*;
import static wegrus.clubwebsite.entity.group.GroupRoles.*;
import static wegrus.clubwebsite.entity.member.MemberRoles.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GroupService {

    private final MemberRepository memberRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupRepository groupRepository;
    private final RoleRepository roleRepository;
    private final MemberRoleRepository memberRoleRepository;

    @Transactional
    public StatusResponse approve(Long groupId, Long applicantId) {
        final Long memberId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        groupRepository.findById(groupId).orElseThrow(GroupNotFoundException::new);
        memberRepository.findById(applicantId).orElseThrow(MemberNotFoundException::new);

        checkExecutiveOrPresident(groupId, memberId);

        final GroupMember groupMember = groupMemberRepository.findByMemberIdAndGroupId(applicantId, groupId).orElseThrow(ApplicantNotFoundException::new);
        if (!groupMember.getRole().equals(APPLICANT))
            throw new GroupMemberAlreadyExistException();
        groupMember.updateRole(MEMBER);

        return new StatusResponse(Status.SUCCESS);
    }

    @Transactional
    public StatusResponse reject(Long groupId, Long applicantId) {
        final Long memberId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        groupRepository.findById(groupId).orElseThrow(GroupNotFoundException::new);
        memberRepository.findById(applicantId).orElseThrow(MemberNotFoundException::new);

        checkExecutiveOrPresident(groupId, memberId);

        final GroupMember groupMember = groupMemberRepository.findByMemberIdAndGroupId(applicantId, groupId).orElseThrow(ApplicantNotFoundException::new);
        if (!groupMember.getRole().equals(APPLICANT))
            throw new GroupMemberAlreadyExistException();
        groupMemberRepository.delete(groupMember);

        return new StatusResponse(Status.SUCCESS);
    }

    private void checkExecutiveOrPresident(Long groupId, Long memberId) {
        final GroupMember groupMember = groupMemberRepository.findByMemberIdAndGroupId(memberId, groupId).orElseThrow(GroupMemberNotFoundException::new);
        if (groupMember.getRole().equals(APPLICANT) || groupMember.getRole().equals(GroupRoles.MEMBER)) {
            final List<ErrorResponse.FieldError> errors = new ArrayList<>();
            errors.add(new ErrorResponse.FieldError("groupRole", EXECUTIVE.name(), INSUFFICIENT_AUTHORITY.getMessage()));
            throw new InsufficientAuthorityException(errors);
        }
    }

    @Transactional
    public StatusResponse promote(Long groupId, Long targetId) {
        final Long memberId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        groupRepository.findById(groupId).orElseThrow(GroupNotFoundException::new);
        final Member target = memberRepository.findById(targetId).orElseThrow(MemberNotFoundException::new);
        final GroupMember president = groupMemberRepository.findByMemberIdAndGroupId(memberId, groupId).orElseThrow(GroupMemberNotFoundException::new);

        checkPresident(president);

        final GroupMember groupMember = groupMemberRepository.findByMemberIdAndGroupId(targetId, groupId).orElseThrow(GroupMemberNotFoundException::new);
        if (!groupMember.getRole().equals(MEMBER))
            throw new GroupMemberCannotPromoteException();

        groupMember.updateRole(EXECUTIVE);
        final Role role = roleRepository.findByName(ROLE_GROUP_EXECUTIVE.name()).orElseThrow(MemberRoleNotFoundException::new);
        if (memberRoleRepository.findByMemberIdAndRoleId(targetId, role.getId()).isEmpty())
            memberRoleRepository.save(new MemberRole(target, role));

        return new StatusResponse(Status.SUCCESS);
    }

    @Transactional
    public StatusResponse degrade(Long groupId, Long targetId) {
        final Long memberId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        groupRepository.findById(groupId).orElseThrow(GroupNotFoundException::new);
        memberRepository.findById(targetId).orElseThrow(MemberNotFoundException::new);
        final GroupMember president = groupMemberRepository.findByMemberIdAndGroupId(memberId, groupId).orElseThrow(GroupMemberNotFoundException::new);

        checkPresident(president);

        final GroupMember groupMember = groupMemberRepository.findByMemberIdAndGroupId(targetId, groupId).orElseThrow(GroupMemberNotFoundException::new);
        if (!groupMember.getRole().equals(EXECUTIVE))
            throw new GroupMemberCannotDegradeException();

        groupMember.updateRole(MEMBER);
        if (groupMemberRepository.findByMemberIdAndRole(targetId, EXECUTIVE).isEmpty()) {
            final Role role = roleRepository.findByName(ROLE_GROUP_EXECUTIVE.name()).orElseThrow(MemberRoleNotFoundException::new);
            memberRoleRepository.deleteByMemberIdAndRoleId(targetId, role.getId());
        }

        return new StatusResponse(Status.SUCCESS);
    }

    @Transactional
    public StatusResponse delegate(Long groupId, Long targetId) {
        final Long memberId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        groupRepository.findById(groupId).orElseThrow(GroupNotFoundException::new);
        final Member target = memberRepository.findById(targetId).orElseThrow(MemberNotFoundException::new);
        final GroupMember president = groupMemberRepository.findByMemberIdAndGroupId(memberId, groupId).orElseThrow(GroupMemberNotFoundException::new);

        checkPresident(president);

        final GroupMember groupMember = groupMemberRepository.findByMemberIdAndGroupId(targetId, groupId).orElseThrow(GroupMemberNotFoundException::new);
        if (!groupMember.getRole().equals(EXECUTIVE) && !groupMember.getRole().equals(MEMBER))
            throw new GroupMemberCannotDelegateException();

        president.updateRole(MEMBER);
        if (groupMemberRepository.findByMemberIdAndRole(memberId, PRESIDENT).isEmpty()) {
            final Role role = roleRepository.findByName(ROLE_GROUP_PRESIDENT.name()).orElseThrow(MemberRoleNotFoundException::new);
            memberRoleRepository.deleteByMemberIdAndRoleId(targetId, role.getId());
        }

        groupMember.updateRole(PRESIDENT);
        if (groupMemberRepository.findByMemberIdAndRole(targetId, PRESIDENT).isEmpty()) {
            final Role role = roleRepository.findByName(ROLE_GROUP_PRESIDENT.name()).orElseThrow(MemberRoleNotFoundException::new);
            memberRoleRepository.save(new MemberRole(target, role));
        }
        if (groupMemberRepository.findByMemberIdAndRole(targetId, EXECUTIVE).isEmpty()) {
            final Role role = roleRepository.findByName(ROLE_GROUP_EXECUTIVE.name()).orElseThrow(MemberRoleNotFoundException::new);
            memberRoleRepository.deleteByMemberIdAndRoleId(targetId, role.getId());
        }

        return new StatusResponse(Status.SUCCESS);
    }

    @Transactional
    public StatusResponse kickMember(Long groupId, Long targetId) {
        final Long memberId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        groupRepository.findById(groupId).orElseThrow(GroupNotFoundException::new);
        memberRepository.findById(targetId).orElseThrow(MemberNotFoundException::new);
        final GroupMember president = groupMemberRepository.findByMemberIdAndGroupId(memberId, groupId).orElseThrow(GroupMemberNotFoundException::new);

        checkPresident(president);

        final GroupMember groupMember = groupMemberRepository.findByMemberIdAndGroupId(targetId, groupId).orElseThrow(GroupMemberNotFoundException::new);
        if (groupMember.getRole().equals(PRESIDENT) || groupMember.getRole().equals(APPLICANT))
            throw new GroupMemberCannotKickException();

        groupMemberRepository.delete(groupMember);
        if (groupMemberRepository.findByMemberIdAndRole(targetId, EXECUTIVE).isEmpty()) {
            final Role role = roleRepository.findByName(ROLE_GROUP_EXECUTIVE.name()).orElseThrow(MemberRoleNotFoundException::new);
            memberRoleRepository.deleteByMemberIdAndRoleId(targetId, role.getId());
        }

        return new StatusResponse(Status.SUCCESS);
    }

    private void checkPresident(GroupMember groupMember) {
        if (!groupMember.getRole().equals(PRESIDENT)) {
            final List<ErrorResponse.FieldError> errors = new ArrayList<>();
            errors.add(new ErrorResponse.FieldError("groupRole", PRESIDENT.name(), INSUFFICIENT_AUTHORITY.getMessage()));
            throw new InsufficientAuthorityException(errors);
        }
    }

    public Page<MemberDto> getMemberDtoPage(Long groupId, GroupRoles role, int page, int size, MemberSortType type, Sort.Direction direction) {
        final Long memberId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        final Group group = groupRepository.findById(groupId).orElseThrow(GroupNotFoundException::new);

        checkExecutiveOrPresident(groupId, memberId);

        final List<GroupRoles> roles = new ArrayList<>();
        roles.add(role);
        if (role.equals(MEMBER)) {
            roles.add(EXECUTIVE);
            roles.add(PRESIDENT);
        } else if (roles.equals(EXECUTIVE))
            roles.add(PRESIDENT);

        page = (page == 0 ? 0 : page - 1);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, type.getField()));
        return memberRepository.findMemberDtoPageByGroupAndRole(pageable, group, roles);
    }

    public Page<MemberDto> searchMember(Long groupId, int page, int size, MemberSortType sortType, Sort.Direction direction, MemberSearchType searchType, String word) {
        final Long memberId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        final Group group = groupRepository.findById(groupId).orElseThrow(GroupNotFoundException::new);

        checkExecutiveOrPresident(groupId, memberId);

        page = (page == 0 ? 0 : page - 1);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortType.getField()));
        return memberRepository.findMemberDtoPageByWordContainingAtSearchTypeAndGroup(pageable, group, searchType, word);
    }

    public Page<MemberDto> searchMemberByGender(Long groupId, int page, int size, MemberSortType sortType, Sort.Direction direction, Gender gender) {
        final Long memberId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        final Group group = groupRepository.findById(groupId).orElseThrow(GroupNotFoundException::new);

        checkExecutiveOrPresident(groupId, memberId);

        page = (page == 0 ? 0 : page - 1);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortType.getField()));
        return memberRepository.findMemberDtoPageByGenderAndGroup(pageable, group, gender);
    }

    public Page<MemberDto> searchMemberByAcademicStatus(Long groupId, int page, int size, MemberSortType sortType, Sort.Direction direction, MemberAcademicStatus academicStatus) {
        final Long memberId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        final Group group = groupRepository.findById(groupId).orElseThrow(GroupNotFoundException::new);

        checkExecutiveOrPresident(groupId, memberId);

        page = (page == 0 ? 0 : page - 1);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortType.getField()));
        return memberRepository.findMemberDtoPageByAcademicStatusAndGroup(pageable, group, academicStatus);
    }

    public Page<MemberDto> searchMemberByGrade(Long groupId, int page, int size, MemberSortType sortType, Sort.Direction direction, MemberGrade grade) {
        final Long memberId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        final Group group = groupRepository.findById(groupId).orElseThrow(GroupNotFoundException::new);

        checkExecutiveOrPresident(groupId, memberId);

        page = (page == 0 ? 0 : page - 1);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortType.getField()));
        return memberRepository.findMemberDtoPageByGradeAndGroup(pageable, group, grade);
    }
}
