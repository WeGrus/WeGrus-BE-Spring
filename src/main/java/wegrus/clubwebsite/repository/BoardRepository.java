package wegrus.clubwebsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wegrus.clubwebsite.entity.board.Board;

import java.util.Optional;

public interface BoardRepository extends JpaRepository<Board, Long> {

}
