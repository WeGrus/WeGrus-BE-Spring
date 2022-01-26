package wegrus.clubwebsite.entity.board;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import wegrus.clubwebsite.entity.member.Member;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Entity
@Table(name = "replies")
public class Reply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reply_id", updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_parent_id")
    private Reply parent;

    @OneToMany(mappedBy = "reply")
    private List<CommentLike> commentLikes = new ArrayList<>();

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
    public Reply(Member member, Post post, Reply parent, String content, ReplyState state){
        this.member = member;
        this.post = post;
        this.parent = parent;
        this.content = content;
        this.state = state;
    }
}
