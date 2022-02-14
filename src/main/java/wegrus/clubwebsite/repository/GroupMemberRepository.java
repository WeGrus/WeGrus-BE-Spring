package wegrus.clubwebsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wegrus.clubwebsite.entity.group.GroupMember;
import wegrus.clubwebsite.entity.group.GroupRoles;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {

    Optional<GroupMember> findByMemberIdAndGroupId(Long memberId, Long groupId);

    void deleteAllByMemberId(Long memberId);

    List<GroupMember> findAllByMemberId(Long memberId);

    Optional<GroupMember> findByMemberIdAndRole(Long targetId, GroupRoles role);
}
