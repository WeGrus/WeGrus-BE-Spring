package wegrus.clubwebsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import wegrus.clubwebsite.entity.member.Member;
import wegrus.clubwebsite.entity.post.Post;
import wegrus.clubwebsite.entity.post.View;

import java.util.Optional;

public interface ViewRepository extends JpaRepository<View, Long> {
    Optional<View> findByMemberAndPost(Member member, Post post);

    @Modifying
    @Query("DELETE FROM View v WHERE v.post = :post")
    void deleteViewsByPost(@Param("post") Post post);
}
