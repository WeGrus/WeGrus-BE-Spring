package wegrus.clubwebsite.entity.board;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import wegrus.clubwebsite.dto.board.BoardUpdateRequest;
import wegrus.clubwebsite.entity.member.Member;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Entity
@Table(name = "boards")
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id", updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "board")
    private List<Reply> replies = new ArrayList<>();

    @OneToMany(mappedBy = "board")
    private List<PostLike> postLikes = new ArrayList<>();

    @OneToMany(mappedBy = "board")
    private List<View> views = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "board_category", nullable = false)
    private BoardCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "board_type", nullable = false)
    private BoardType type;

    @Column(name = "board_title", nullable = false)
    private String title;

    @Lob
    @Column(name = "board_content", nullable = false)
    private String content;

    @CreatedDate
    @Column(name = "board_create_date")
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "board_update_date")
    private LocalDateTime updatedDate;

    @Column(name = "board_secret_flag", nullable = false)
    private boolean secretFlag;

    @Enumerated(EnumType.STRING)
    @Column(name = "board_state", nullable = false)
    private BoardState state;

    @Builder
    public Board(Member member, BoardCategory category, BoardType type, String title, String content, boolean secretFlag, BoardState state){
        this.member = member;
        this.category = category;
        this.type = type;
        this.title = title;
        this.content = content;
        this.secretFlag = secretFlag;
        this.state = state;
    }

    public void update(BoardUpdateRequest request){
        this.title = request.getTitle();
        this.content = request.getContent();
        this.secretFlag = request.isSecretFlag();
    }
}
