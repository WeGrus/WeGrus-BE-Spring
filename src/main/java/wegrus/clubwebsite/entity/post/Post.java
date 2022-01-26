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

    @OneToMany(mappedBy = "post")
    private List<Reply> replies = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    private List<PostLike> postLikes = new ArrayList<>();

    @OneToMany(mappedBy = "post")
    private List<View> views = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "post_category", nullable = false)
    private BoardCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false)
    private BoardType type;

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

    @Builder
    public Post(Member member, BoardCategory category, BoardType type, String title, String content, boolean secretFlag, PostState state){
        this.member = member;
        this.category = category;
        this.type = type;
        this.title = title;
        this.content = content;
        this.secretFlag = secretFlag;
        this.state = state;
    }

    public void update(PostUpdateRequest request){
        this.title = request.getTitle();
        this.content = request.getContent();
        this.secretFlag = request.isSecretFlag();
    }
}
