package wegrus.clubwebsite.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import wegrus.clubwebsite.dto.member.*;
import wegrus.clubwebsite.entity.group.Group;
import wegrus.clubwebsite.entity.group.GroupRoles;
import wegrus.clubwebsite.entity.member.Gender;
import wegrus.clubwebsite.entity.member.MemberAcademicStatus;
import wegrus.clubwebsite.entity.member.MemberGrade;

import java.util.List;
import java.util.Optional;

public interface MemberRepositoryQuerydsl {

    Optional<MemberDto> findMemberDtoById(Long id);

    Optional<MemberSimpleDto> findMemberSimpleDtoById(Long id);

    Page<MemberDto> findMemberDtoPage(Pageable pageable);

    Page<MemberDto> findMemberDtoPageByWordContainingAtSearchType(Pageable pageable, MemberSearchType searchType, String word);

    Page<MemberDto> findMemberDtoPageByGender(Pageable pageable, Gender gender);

    Page<MemberDto> findMemberDtoPageByAcademicStatus(Pageable pageable, MemberAcademicStatus academicStatus);

    Page<MemberDto> findMemberDtoPageByGrade(Pageable pageable, MemberGrade grade);

    Page<MemberDto> findMemberDtoPageByAuthority(Pageable pageable, MemberRoleSearchType authority);

    Page<MemberDto> findMemberDtoPageByGroup(Pageable pageable, Long groupId);

    Page<MemberDto> findMemberDtoPageByGroupAndRole(Pageable pageable, Group group, List<GroupRoles> roles);

    Page<MemberDto> findMemberDtoPageByWordContainingAtRequesterSearchType(Pageable pageable, RequesterSearchType type, String word);

    Page<MemberDto> findMemberDtoPageByWordContainingAtSearchTypeAndGroup(Pageable pageable, Group group, MemberSearchType searchType, String word);

    Page<MemberDto> findMemberDtoPageByGenderAndGroup(Pageable pageable, Group group, Gender gender);

    Page<MemberDto> findMemberDtoPageByAcademicStatusAndGroup(Pageable pageable, Group group, MemberAcademicStatus academicStatus);

    Page<MemberDto> findMemberDtoPageByGradeAndGroup(Pageable pageable, Group group, MemberGrade grade);
}
