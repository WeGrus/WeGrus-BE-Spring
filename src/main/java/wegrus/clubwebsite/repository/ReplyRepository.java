package wegrus.clubwebsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import wegrus.clubwebsite.entity.post.Post;
import wegrus.clubwebsite.entity.post.Reply;

public interface ReplyRepository extends JpaRepository<Reply, Long> {

    @Modifying
    @Query("DELETE FROM Reply p WHERE p.post = :post")
    void deleteRepliesByPost(@Param("post") Post post);
}
