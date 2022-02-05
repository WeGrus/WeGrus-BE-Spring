package wegrus.clubwebsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import wegrus.clubwebsite.entity.member.Member;
import wegrus.clubwebsite.entity.post.Bookmark;
import wegrus.clubwebsite.entity.post.Post;

import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Optional<Bookmark> findByMemberAndPost(Member member, Post post);

    @Modifying
    @Query("DELETE FROM Bookmark b WHERE b.post = :post")
    void deleteBookmarksByPost(@Param("post") Post post);
}
