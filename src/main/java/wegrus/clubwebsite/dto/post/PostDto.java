package wegrus.clubwebsite.dto.post;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import wegrus.clubwebsite.entity.post.Post;
import wegrus.clubwebsite.vo.Image;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class PostDto {

    private Long postId;
    private Long memberId;
    private String memberName;
    private Image image;
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

    public PostDto(Post post) {
        this.postId = post.getId();
        this.memberId = post.getMember().getId();
        this.memberName = post.getMember().getStudentId().substring(2, 4) + post.getMember().getName();
        this.image = post.getMember().getImage();
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

    @QueryProjection
    public PostDto(Long postId, Long memberId, String memberName, Image image, String board, String boardCategory, String type, String title, String content, LocalDateTime createdDate, LocalDateTime updatedDate, Integer postLike, Integer postReplies, Integer postView, Integer postBookmarks, boolean secretFlag) {
        this.postId = postId;
        this.memberId = memberId;
        this.memberName = memberName;
        this.image = image;
        this.board = board;
        this.boardCategory = boardCategory;
        this.type = type;
        this.title = title;
        this.content = content;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
        this.postLike = postLike;
        this.postReplies = postReplies;
        this.postView = postView;
        this.postBookmarks = postBookmarks;
        this.secretFlag = secretFlag;
    }
}
