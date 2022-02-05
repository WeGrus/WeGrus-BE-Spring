package wegrus.clubwebsite.dto.post;

import lombok.Getter;
import lombok.NoArgsConstructor;
import wegrus.clubwebsite.entity.post.Post;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class PostUnknownDto {
    private Long postId;
    private Long memberId;
    private String memberName;
    private String board;
    private String boardCategory;
    private String type;
    private String title;
    private String content;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private Integer postLike;
    private Integer postReplies;
    private Integer postView;
    private Integer postBookmarks;
    private boolean secretFlag;

    public PostUnknownDto(Post post){
        this.postId = post.getId();
        this.memberId = post.getMember().getId();
        this.memberName = "알 수 없음";
        this.board = post.getBoard().getName();
        this.boardCategory = post.getBoard().getBoardCategory().getName();
        this.type = post.getType().toString();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.createdDate = post.getCreatedDate();
        this.updatedDate = post.getUpdatedDate();
        this.postLike = post.getPostLikeNum();
        this.postReplies = post.getPostReplyNum();
        this.postView = post.getViews().size();
        this.postBookmarks = post.getBookmarks().size();
        this.secretFlag = post.isSecretFlag();
    }
}
