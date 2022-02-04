package wegrus.clubwebsite.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import wegrus.clubwebsite.entity.post.Board;
import wegrus.clubwebsite.entity.post.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByBoardOrderByTypeDescIdDesc(Board board, Pageable pageable);

    Page<Post> findByBoardOrderByTypeDescPostLikeNumDescIdDesc(Board board, Pageable pageable);

    Page<Post> findByBoardOrderByTypeDescReplyNumDescIdDesc(Board board, Pageable pageable);

    Page<Post> findByBoardAndTitleContainingIgnoreCaseOrderByTypeDescIdDesc(Board board, String title, Pageable pageable);

    Page<Post> findByBoardAndTitleContainingIgnoreCaseOrderByTypeDescPostLikeNumDescIdDesc(Board board, String title, Pageable pageable);

    Page<Post> findByBoardAndTitleContainingIgnoreCaseOrderByTypeDescReplyNumDescIdDesc(Board board, String title, Pageable pageable);

    Page<Post> findByBoardAndMemberNameContainingIgnoreCaseOrderByTypeDescIdDesc(Board board, String writer, Pageable pageable);

    Page<Post> findByBoardAndMemberNameContainingIgnoreCaseOrderByTypeDescPostLikeNumDescIdDesc(Board board, String writer, Pageable pageable);

    Page<Post> findByBoardAndMemberNameContainingIgnoreCaseOrderByTypeDescReplyNumDescIdDesc(Board board, String writer, Pageable pageable);
}
