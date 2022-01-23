package wegrus.clubwebsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import wegrus.clubwebsite.entity.board.CommentLike;
import wegrus.clubwebsite.entity.board.Reply;
import wegrus.clubwebsite.entity.member.Member;

import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    Optional<CommentLike> findByMemberAndReply(Member member, Reply reply);

    @Modifying
    @Query("DELETE FROM CommentLike c WHERE c.reply = :reply")
    void deleteCommentLikesByReply(@Param("reply") Reply reply);

    @Modifying
    @Query(value = "DELETE FROM b " +
            "USING replies AS a " +
            "INNER JOIN comment_likes AS b " +
            "ON a.reply_id = b.reply_id " +
            "WHERE a.board_id = :board", nativeQuery = true)
    void deleteCommentLikesByBoard(@Param("board") Long boardId);
}
