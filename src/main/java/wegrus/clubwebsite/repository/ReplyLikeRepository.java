package wegrus.clubwebsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import wegrus.clubwebsite.entity.post.ReplyLike;
import wegrus.clubwebsite.entity.post.Reply;
import wegrus.clubwebsite.entity.member.Member;

import java.util.Optional;

public interface ReplyLikeRepository extends JpaRepository<ReplyLike, Long> {
    Optional<ReplyLike> findByMemberAndReply(Member member, Reply reply);

    @Modifying
    @Query("DELETE FROM ReplyLike c WHERE c.reply = :reply")
    void deleteReplyLikesByReply(@Param("reply") Reply reply);

    @Modifying
    @Query(value = "DELETE FROM b " +
            "USING replies AS a " +
            "INNER JOIN reply_likes AS b " +
            "ON a.reply_id = b.reply_id " +
            "WHERE a.post_id = :post", nativeQuery = true)
    void deleteReplyLikesByPost(@Param("post") Long postId);
}
