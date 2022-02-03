package wegrus.clubwebsite.dto.post;

import lombok.Getter;
import lombok.NoArgsConstructor;
import wegrus.clubwebsite.entity.post.Reply;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ReplyDto {

    private Long replyId;
    private Long memberId;
    private String memberName;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private Long replyLike;
    private String content;

    public ReplyDto(Reply reply){
        this.replyId = reply.getId();
        this.memberId = reply.getMember().getId();
        this.memberName = reply.getMember().getName();
        this.createdDate = reply.getCreatedDate();
        this.updatedDate = reply.getUpdatedDate();
        this.replyLike = (long) reply.getReplyLikes().size();
        this.content = reply.getContent();
    }
}
