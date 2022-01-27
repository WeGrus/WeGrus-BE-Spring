package wegrus.clubwebsite.entity.post;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "board_categories")
public class BoardCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_category_id", insertable = false, updatable = false)
    private Long id;

    @OneToMany(mappedBy = "boardCategory")
    private List<Board> boards = new ArrayList<>();

    @Column(name = "board_category_name", nullable = false)
    private String name;

    @Builder
    public BoardCategory(String name){
        this.name = name;
    }
}
