package wegrus.clubwebsite.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import wegrus.clubwebsite.dto.member.MemberDto;
import wegrus.clubwebsite.dto.member.MemberSearchType;

public interface MemberRepositoryQuerydsl {

    Page<MemberDto> findMemberDtoPage(Pageable pageable);

    Page<MemberDto> findMemberDtoPageByWordContainingAtSearchType(Pageable pageable, MemberSearchType searchType, String word);
}
