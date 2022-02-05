package wegrus.clubwebsite.entity.post;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import wegrus.clubwebsite.dto.post.PostUpdateRequest;
import wegrus.clubwebsite.entity.member.Member;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id", insertable = false, updatable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @OneToMany(mappedBy = "post")
    private List<Reply> replies = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    private List<PostLike> postLikes = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    private List<View> views = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type")
    private PostType type;

    @Column(name = "post_title", nullable = false)
    private String title;

    @Lob
    @Column(name = "post_content", nullable = false)
    private String content;

    @CreatedDate
    @Column(name = "post_create_date")
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "post_update_date")
    private LocalDateTime updatedDate;

    @Column(name = "post_secret_flag", nullable = false)
    private boolean secretFlag;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_state", nullable = false)
    private PostState state;

    @Column(name = "post_like_num")
    private Integer postLikeNum;

    @Column(name = "post_reply_num")
    private Integer postReplyNum;

    @Builder
    public Post(Member member, Board board, PostType type, String title, String content, boolean secretFlag, PostState state) {
        this.member = member;
        this.board = board;
        this.type = type;
        this.title = title;
        this.content = content;
        this.secretFlag = secretFlag;
        this.state = state;
        this.postLikeNum = 0;
        this.postReplyNum = 0;
    }

    public void update(PostUpdateRequest request) {
        this.title = request.getTitle();
        this.content = request.getContent();
        this.secretFlag = request.isSecretFlag();
    }

    public void likeNum(Integer postLikeNum) {
        this.postLikeNum = postLikeNum;
    }

    public void postReplyNum(Integer postReplyNum) {
        this.postReplyNum = postReplyNum;
    }
}
