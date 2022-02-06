package wegrus.clubwebsite.dto.post;

import lombok.Getter;
import lombok.NoArgsConstructor;
import wegrus.clubwebsite.entity.post.Reply;
import wegrus.clubwebsite.vo.Image;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class PostReplyDto {

    private Long replyId;
    private String content;
    private Long memberId;
    private String memberName;
    private Image memberImage;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private PostDto post;

    public PostReplyDto(Reply reply, PostDto post) {
        this.replyId = reply.getId();
        this.content = reply.getContent();
        this.memberId = reply.getMember().getId();
        this.memberName = reply.getMember().getStudentId().substring(2, 4).concat(reply.getMember().getName());
        this.memberImage = reply.getMember().getImage();
        this.createdDate = reply.getCreatedDate();
        this.updatedDate = reply.getUpdatedDate();
        this.post = post;
    }
}
