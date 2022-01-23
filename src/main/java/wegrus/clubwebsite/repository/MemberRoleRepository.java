package wegrus.clubwebsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wegrus.clubwebsite.entity.member.MemberRole;

import java.util.Optional;

public interface MemberRoleRepository extends JpaRepository<MemberRole, Long> {
    Optional<MemberRole> findByMemberIdAndRoleId(Long memberId, Long roleId);
}
