package wegrus.clubwebsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wegrus.clubwebsite.entity.board.CommentLike;

public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {
}
