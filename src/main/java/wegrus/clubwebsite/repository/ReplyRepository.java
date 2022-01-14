package wegrus.clubwebsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wegrus.clubwebsite.entity.board.Reply;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
}
