package wegrus.clubwebsite.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Entity
@Table(name = "replys")
public class Reply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reply_id", updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_parent_id")
    private Reply parent;

    @JsonIgnore
    @OneToMany(mappedBy = "reply")
    private List<CommentLike> commentLikes = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "parent")
    private List<Reply> replies = new ArrayList<>();

    @Column(name = "reply_content", nullable = false)
    private String content;

    @CreatedDate
    @Column(name = "reply_create_date", nullable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "reply_update_date", nullable = false)
    private LocalDateTime updatedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "reply_state", nullable = false)
    private ReplyState state;

    @Builder
    public Reply(Member member, Board board, Reply parent, String content, ReplyState state){
        this.member = member;
        this.board = board;
        this.parent = parent;
        this.content = content;
        this.state = state;
    }
}
