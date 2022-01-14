package wegrus.clubwebsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wegrus.clubwebsite.entity.board.Board;

public interface BoardRepository extends JpaRepository<Board, Long> {
}
