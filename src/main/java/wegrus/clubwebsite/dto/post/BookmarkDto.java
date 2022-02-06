package wegrus.clubwebsite.dto.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BookmarkDto {

    private Long bookmarkId;
    private PostDto post;
}
