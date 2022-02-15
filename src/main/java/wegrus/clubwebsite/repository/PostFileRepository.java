package wegrus.clubwebsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wegrus.clubwebsite.entity.post.PostFile;

public interface PostFileRepository extends JpaRepository<PostFile, Long> {
}
