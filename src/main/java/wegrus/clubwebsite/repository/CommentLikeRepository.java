package wegrus.clubwebsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wegrus.clubwebsite.entity.board.CommentLike;
import wegrus.clubwebsite.entity.board.Reply;
import wegrus.clubwebsite.entity.member.Member;

import java.util.Optional;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
    Optional<CommentLike> findByMemberAndReply(Member member, Reply reply);
}
