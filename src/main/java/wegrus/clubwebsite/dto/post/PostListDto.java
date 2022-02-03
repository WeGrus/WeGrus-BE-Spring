package wegrus.clubwebsite.dto.post;

import lombok.Getter;
import lombok.NoArgsConstructor;
import wegrus.clubwebsite.entity.post.Post;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class PostListDto {
    private Long postId;
    private Long memberId;
    private String memberName;
    private String board;
    private String boardCategory;
    private String type;
    private String title;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private Integer postLike;
    private Integer postView;
    private Integer postReplies;
    private boolean secretFlag;

    public PostListDto(Post post){
        this.postId = post.getId();
        this.memberId = post.getMember().getId();
        this.memberName = post.getMember().getStudentId().substring(2, 4) + post.getMember().getName();
        this.board = post.getBoard().getName();
        this.boardCategory = post.getBoard().getBoardCategory().getName();
        this.type = post.getType().toString();
        this.title = post.getTitle();
        this.createdDate = post.getCreatedDate();
        this.updatedDate = post.getUpdatedDate();
        this.postLike = post.getPostLikeNum();
        this.postView = post.getViews().size();
        this.postReplies = post.getReplyNum();
        this.secretFlag = post.isSecretFlag();
    }
}
