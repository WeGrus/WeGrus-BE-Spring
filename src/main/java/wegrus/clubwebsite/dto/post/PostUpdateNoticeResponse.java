package wegrus.clubwebsite.dto.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import wegrus.clubwebsite.entity.post.PostType;

@Getter
@AllArgsConstructor
public class PostUpdateNoticeResponse {
    private Long postId;
    private PostType type;
}
