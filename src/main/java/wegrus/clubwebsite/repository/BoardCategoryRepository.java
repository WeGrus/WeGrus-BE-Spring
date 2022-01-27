package wegrus.clubwebsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wegrus.clubwebsite.entity.post.BoardCategory;

public interface BoardCategoryRepository extends JpaRepository<BoardCategory, Long> {
}
