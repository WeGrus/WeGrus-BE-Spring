package wegrus.clubwebsite.dto.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@AllArgsConstructor
public class PostListResponse {
    private Page<PostListDto> posts;
}
