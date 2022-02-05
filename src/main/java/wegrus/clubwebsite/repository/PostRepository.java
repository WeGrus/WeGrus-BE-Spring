package wegrus.clubwebsite.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import wegrus.clubwebsite.entity.post.Board;
import wegrus.clubwebsite.entity.post.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findByBoardOrderByTypeDescIdDesc(Board board, Pageable pageable);

    Page<Post> findByBoardOrderByTypeDescPostLikeNumDescIdDesc(Board board, Pageable pageable);

    Page<Post> findByBoardOrderByTypeDescPostReplyNumDescIdDesc(Board board, Pageable pageable);

    Page<Post> findByBoardAndTitleContainingIgnoreCaseOrderByTypeDescIdDesc(Board board, String title, Pageable pageable);

    Page<Post> findByBoardAndTitleContainingIgnoreCaseOrderByTypeDescPostLikeNumDescIdDesc(Board board, String title, Pageable pageable);

    Page<Post> findByBoardAndTitleContainingIgnoreCaseOrderByTypeDescPostReplyNumDescIdDesc(Board board, String title, Pageable pageable);

    Page<Post> findByBoardAndMemberNameContainingIgnoreCaseOrderByTypeDescIdDesc(Board board, String writer, Pageable pageable);

    Page<Post> findByBoardAndMemberNameContainingIgnoreCaseOrderByTypeDescPostLikeNumDescIdDesc(Board board, String writer, Pageable pageable);

    Page<Post> findByBoardAndMemberNameContainingIgnoreCaseOrderByTypeDescPostReplyNumDescIdDesc(Board board, String writer, Pageable pageable);

    Page<Post> findByBoardAndTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrderByTypeDescIdDesc(Board board, String title, String content, Pageable pageable);

    Page<Post> findByBoardAndTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrderByTypeDescPostLikeNumDescIdDesc(Board board, String title, String content, Pageable pageable);

    Page<Post> findByBoardAndTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrderByTypeDescPostReplyNumDescIdDesc(Board board, String title, String content, Pageable pageable);
}
