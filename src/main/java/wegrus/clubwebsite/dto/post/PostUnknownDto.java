package wegrus.clubwebsite.dto.post;

import lombok.Getter;
import lombok.NoArgsConstructor;
import wegrus.clubwebsite.entity.post.Post;

import java.time.format.DateTimeFormatter;
import java.util.List;

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
    private String createdDate;
    private String updatedDate;
    private Integer postLike;
    private Integer postReplies;
    private Integer postView;
    private Integer postBookmarks;
    private List<String> postFileUrls;
    private boolean userPostLiked;
    private boolean userPostBookmarked;
    private boolean secretFlag;

    public PostUnknownDto(Post post, boolean userPostLiked, boolean userPostBookmarked, List<String> postFileUrls) {
        this.postId = post.getId();
        this.memberId = post.getMember().getId();
        this.memberName = "알 수 없음";
        this.board = post.getBoard().getName();
        this.boardCategory = post.getBoard().getBoardCategory().getName();
        this.type = post.getType().toString();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.createdDate = post.getCreatedDate().format(DateTimeFormatter.ofPattern("yy/MM/dd|HH:mm:ss"));
        this.updatedDate = post.getUpdatedDate().format(DateTimeFormatter.ofPattern("yy/MM/dd|HH:mm:ss"));
        this.postLike = post.getPostLikeNum();
        this.postReplies = post.getPostReplyNum();
        this.postView = post.getViews().size();
        this.postBookmarks = post.getBookmarks().size();
        this.postFileUrls = postFileUrls;
        this.userPostLiked = userPostLiked;
        this.userPostBookmarked = userPostBookmarked;
        this.secretFlag = post.isSecretFlag();
    }
}
