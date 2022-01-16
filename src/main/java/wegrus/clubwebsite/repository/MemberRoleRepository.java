package wegrus.clubwebsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wegrus.clubwebsite.entity.member.MemberRole;

public interface MemberRoleRepository extends JpaRepository<MemberRole, Long> {
}
