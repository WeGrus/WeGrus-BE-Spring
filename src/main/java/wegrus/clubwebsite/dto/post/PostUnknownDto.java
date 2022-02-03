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
    private Long postLike;
    private Long postView;
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
        this.postLike = (long) post.getReplies().size();
        this.postView = (long) post.getViews().size();
        this.secretFlag = post.isSecretFlag();
    }
}
