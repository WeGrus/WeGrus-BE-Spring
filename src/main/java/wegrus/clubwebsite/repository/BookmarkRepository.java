package wegrus.clubwebsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wegrus.clubwebsite.entity.member.Member;
import wegrus.clubwebsite.entity.post.Bookmark;
import wegrus.clubwebsite.entity.post.Post;

import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Optional<Bookmark> findByMemberAndPost(Member member, Post post);
}
