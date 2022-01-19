package wegrus.clubwebsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import wegrus.clubwebsite.entity.member.Member;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByKakaoIdOrEmail(Long kakaoId, String email);
    Optional<Member> findByEmail(String email);
    Optional<Member> findByKakaoId(Long kakaoId);
    Optional<Member> findById(Long Id);
}
