package wegrus.clubwebsite.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import wegrus.clubwebsite.dto.member.RequestDto;
import wegrus.clubwebsite.entity.member.MemberRoles;

public interface RequestRepositoryQuerydsl {

    Page<RequestDto> findRequestDtoPageByRole(MemberRoles role, Pageable pageable);
}
