package wegrus.clubwebsite.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import wegrus.clubwebsite.dto.member.MemberDto;
import wegrus.clubwebsite.dto.member.MemberRoleSearchType;
import wegrus.clubwebsite.dto.member.MemberSearchType;
import wegrus.clubwebsite.dto.member.MemberSimpleDto;
import wegrus.clubwebsite.entity.member.Gender;
import wegrus.clubwebsite.entity.member.MemberAcademicStatus;
import wegrus.clubwebsite.entity.member.MemberGrade;

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
}
