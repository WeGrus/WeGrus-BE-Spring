package wegrus.clubwebsite.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import wegrus.clubwebsite.entity.group.Group;

public interface GroupRepository extends JpaRepository<Group, Long> {
}
