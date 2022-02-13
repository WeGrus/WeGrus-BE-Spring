package wegrus.clubwebsite.dto.post;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostImageCreateResponse {
    private String imageUrl;
    private Long postImageId;
}
