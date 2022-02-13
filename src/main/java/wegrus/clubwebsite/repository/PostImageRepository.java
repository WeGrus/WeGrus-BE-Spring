package wegrus.clubwebsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import wegrus.clubwebsite.entity.post.Post;
import wegrus.clubwebsite.entity.post.PostImage;

public interface PostImageRepository extends JpaRepository<PostImage, Long> {
    @Modifying
    @Query("DELETE FROM PostImage p WHERE p.post = :post")
    void deletePostImagesByPost(@Param("post") Post post);

}
