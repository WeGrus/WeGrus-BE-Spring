package wegrus.clubwebsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import wegrus.clubwebsite.entity.post.Post;
import wegrus.clubwebsite.entity.post.PostFile;

import java.util.Optional;

public interface PostFileRepository extends JpaRepository<PostFile, Long> {
    Optional<PostFile> findByPostId(Long postId);

    @Modifying
    @Query("DELETE FROM PostFile p WHERE p.post = :post")
    void deletePostFilesByPost(@Param("post") Post post);
}
