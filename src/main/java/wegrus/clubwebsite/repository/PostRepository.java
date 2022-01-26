package wegrus.clubwebsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wegrus.clubwebsite.entity.post.Post;

public interface PostRepository extends JpaRepository<Post, Long> {

}
