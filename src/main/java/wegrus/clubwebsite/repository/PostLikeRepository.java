package wegrus.clubwebsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wegrus.clubwebsite.entity.board.Board;
import wegrus.clubwebsite.entity.board.PostLike;
import wegrus.clubwebsite.entity.member.Member;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByMemberAndBoard(Member member, Board board);
}
