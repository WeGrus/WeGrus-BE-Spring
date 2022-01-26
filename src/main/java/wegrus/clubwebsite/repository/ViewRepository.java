package wegrus.clubwebsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wegrus.clubwebsite.entity.post.View;

public interface ViewRepository extends JpaRepository<View, Long> {
}
