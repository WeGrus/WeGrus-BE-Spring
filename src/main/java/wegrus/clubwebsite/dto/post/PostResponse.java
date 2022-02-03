package wegrus.clubwebsite.dto.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostResponse {
    private Object board;
    private List<ReplyDto> replies;

    public PostResponse(PostDto board, List<ReplyDto> replies){
        this.board = board;
        this.replies = replies;
    }

    public PostResponse(PostUnknownDto board, List<ReplyDto> replies){
        this.board = board;
        this.replies = replies;
    }

}
