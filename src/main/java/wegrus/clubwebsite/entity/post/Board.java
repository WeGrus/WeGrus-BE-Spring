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
@Table(name = "boards")
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id", insertable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_category_id", nullable = false)
    private BoardCategory boardCategory;

    @OneToMany(mappedBy = "board")
    private List<Post> posts = new ArrayList<>();

    @Column(name = "board_name", nullable = false)
    private String name;

    @Column(name = "board_secret_flag", nullable = false)
    private boolean secretFlag;

    @Builder
    public Board(BoardCategory boardCategory, String name, boolean secretFlag) {
        this.boardCategory = boardCategory;
        this.name = name;
        this.secretFlag = secretFlag;
    }
}
