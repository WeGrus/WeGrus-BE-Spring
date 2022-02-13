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
import wegrus.clubwebsite.dto.member.MemberSortType;
import wegrus.clubwebsite.entity.group.Group;
import wegrus.clubwebsite.entity.group.GroupMember;
import wegrus.clubwebsite.entity.group.GroupRoles;
import wegrus.clubwebsite.exception.*;
import wegrus.clubwebsite.repository.GroupMemberRepository;
import wegrus.clubwebsite.repository.GroupRepository;
import wegrus.clubwebsite.repository.MemberRepository;

import java.util.ArrayList;
import java.util.List;

import static wegrus.clubwebsite.dto.error.ErrorCode.*;
import static wegrus.clubwebsite.entity.group.GroupRoles.*;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GroupService {

    private final MemberRepository memberRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupRepository groupRepository;

    public Page<MemberDto> getApplicants(Long groupId, int page, int size, MemberSortType sortType, Sort.Direction direction) {
        final Long memberId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        final Group group = groupRepository.findById(groupId).orElseThrow(GroupNotFoundException::new);

        validateGroupRole(groupId, memberId);

        page = (page == 0 ? 0 : page - 1);
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortType.getField()));
        return memberRepository.findMemberDtoPageByGroupAndRole(pageable, group, APPLICANT);
    }

    @Transactional
    public StatusResponse approve(Long groupId, Long applicantId) {
        final Long memberId = Long.valueOf(SecurityContextHolder.getContext().getAuthentication().getName());
        groupRepository.findById(groupId).orElseThrow(GroupNotFoundException::new);
        memberRepository.findById(applicantId).orElseThrow(MemberNotFoundException::new);

        validateGroupRole(groupId, memberId);

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

        validateGroupRole(groupId, memberId);

        final GroupMember groupMember = groupMemberRepository.findByMemberIdAndGroupId(applicantId, groupId).orElseThrow(ApplicantNotFoundException::new);
        if (!groupMember.getRole().equals(APPLICANT))
            throw new GroupMemberAlreadyExistException();
        groupMemberRepository.delete(groupMember);

        return new StatusResponse(Status.SUCCESS);
    }

    private void validateGroupRole(Long groupId, Long memberId) {
        final GroupMember groupMember = groupMemberRepository.findByMemberIdAndGroupId(memberId, groupId).orElseThrow(GroupMemberNotFoundException::new);
        if (groupMember.getRole().equals(APPLICANT) || groupMember.getRole().equals(GroupRoles.MEMBER)) {
            final List<ErrorResponse.FieldError> errors = new ArrayList<>();
            errors.add(new ErrorResponse.FieldError("groupRole", GroupRoles.EXECUTIVE.name(), INSUFFICIENT_AUTHORITY.getMessage()));
            throw new InsufficientAuthorityException(errors);
        }
    }

}
