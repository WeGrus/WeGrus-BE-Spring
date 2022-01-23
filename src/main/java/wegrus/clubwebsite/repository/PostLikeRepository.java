package wegrus.clubwebsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import wegrus.clubwebsite.entity.board.Board;
import wegrus.clubwebsite.entity.board.PostLike;
import wegrus.clubwebsite.entity.member.Member;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByMemberAndBoard(Member member, Board board);

    @Modifying
    @Query("DELETE FROM PostLike p WHERE p.board = :board")
    void deletePostLikesByBoard(@Param("board")Board board);
}
