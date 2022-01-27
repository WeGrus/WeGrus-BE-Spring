package wegrus.clubwebsite.dto.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostResponse {
    private PostDto board;
    private List<ReplyDto> replies;
}
