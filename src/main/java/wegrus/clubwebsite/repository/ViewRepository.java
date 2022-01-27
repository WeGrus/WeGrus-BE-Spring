package wegrus.clubwebsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wegrus.clubwebsite.entity.member.Member;
import wegrus.clubwebsite.entity.post.Post;
import wegrus.clubwebsite.entity.post.View;

import java.util.Optional;

public interface ViewRepository extends JpaRepository<View, Long> {
    Optional<View> findByMemberAndPost(Member member, Post post);
}
