package wegrus.clubwebsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wegrus.clubwebsite.entity.board.PostLike;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
}
