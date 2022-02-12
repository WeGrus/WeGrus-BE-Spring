package wegrus.clubwebsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import wegrus.clubwebsite.entity.member.MemberRole;

import java.util.List;
import java.util.Optional;

public interface MemberRoleRepository extends JpaRepository<MemberRole, Long> {

    Optional<MemberRole> findByMemberIdAndRoleId(Long memberId, Long roleId);

    List<MemberRole> findAllByMemberId(Long memberId);

    void deleteAllByMemberId(Long memberId);

    @Query("select mr from MemberRole mr join fetch mr.role where mr.member.id = :memberId")
    List<MemberRole> findAllWithRoleByMemberId(@Param("memberId") Long memberId);

    void deleteByMemberIdAndRoleId(Long presidentId, Long roleId);

    void deleteAllInBatchByRoleIdNotIn(List<Long> roleIds);

    List<MemberRole> findAllByRoleIdIn(List<Long> roleIds);
}
