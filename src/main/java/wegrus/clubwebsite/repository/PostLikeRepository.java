package wegrus.clubwebsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wegrus.clubwebsite.entity.PostLike;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
}
