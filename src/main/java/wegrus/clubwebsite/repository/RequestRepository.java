package wegrus.clubwebsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import wegrus.clubwebsite.entity.Request;

import java.util.Optional;

public interface RequestRepository extends JpaRepository<Request, Long> {
    void deleteByMemberIdAndRoleId(Long memberId, Long roleId);
    @Query("select r from Request r join fetch r.role where r.id = :id")
    Optional<Request> findWithRoleById(@Param("id") Long id);
    Optional<Request> findByMemberIdAndRoleId(Long memberId, Long roleId);
}
