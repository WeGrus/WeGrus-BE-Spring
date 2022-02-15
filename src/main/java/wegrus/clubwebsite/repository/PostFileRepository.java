package wegrus.clubwebsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wegrus.clubwebsite.entity.post.PostFile;

import java.util.Optional;

public interface PostFileRepository extends JpaRepository<PostFile, Long> {
    Optional<PostFile> findByPostId(Long postId);
}
