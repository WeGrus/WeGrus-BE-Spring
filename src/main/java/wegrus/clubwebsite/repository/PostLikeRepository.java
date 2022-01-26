package wegrus.clubwebsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import wegrus.clubwebsite.entity.board.Post;
import wegrus.clubwebsite.entity.board.PostLike;
import wegrus.clubwebsite.entity.member.Member;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByMemberAndPost(Member member, Post post);

    @Modifying
    @Query("DELETE FROM PostLike p WHERE p.post = :post")
    void deletePostLikesByPost(@Param("post") Post post);
}
