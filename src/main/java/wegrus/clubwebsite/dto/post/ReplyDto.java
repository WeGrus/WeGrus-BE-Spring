package wegrus.clubwebsite.dto.post;

import lombok.Getter;
import lombok.NoArgsConstructor;
import wegrus.clubwebsite.entity.member.Member;
import wegrus.clubwebsite.entity.post.Reply;
import wegrus.clubwebsite.vo.Image;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class ReplyDto {

    private Long replyId;
    private Long memberId;
    private Image image;
    private Long replyParentId;
    private String memberName;
    private String createdDate;
    private String updatedDate;
    private Long replyLike;
    private String content;
    private boolean userReplyLiked;

    public ReplyDto(Reply reply, Long memberId) {
        this.replyId = reply.getId();
        this.memberId = reply.getMember().getId();
        this.image = reply.getMember().getImage();
        this.replyParentId = reply.getParent() == null ? -1L : reply.getParent().getId();
        this.memberName = reply.getMember().getName();
        this.createdDate = reply.getCreatedDate().format(DateTimeFormatter.ofPattern("yy/MM/dd|HH:mm:ss"));
        this.updatedDate = reply.getUpdatedDate().format(DateTimeFormatter.ofPattern("yy/MM/dd|HH:mm:ss"));
        this.replyLike = (long) reply.getReplyLikes().size();
        this.content = reply.getContent();
        this.userReplyLiked = reply.getReplyLikes().stream().anyMatch(r -> r.getMember().getId().equals(memberId));
    }
}
