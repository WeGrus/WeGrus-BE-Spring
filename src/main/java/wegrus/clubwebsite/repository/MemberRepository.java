package wegrus.clubwebsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import wegrus.clubwebsite.entity.member.Member;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryQuerydsl {

    Optional<Member> findByUserIdOrEmail(String userId, String email);

    Optional<Member> findByEmail(String email);

    Optional<Member> findByUserId(String userId);

    @Query("select m from Member m join fetch m.roles r join fetch r.role where m.id = :id")
    Optional<Member> findWithMemberRolesAndRoleById(@Param("id") Long id);
}
