package wegrus.clubwebsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wegrus.clubwebsite.entity.board.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

}
