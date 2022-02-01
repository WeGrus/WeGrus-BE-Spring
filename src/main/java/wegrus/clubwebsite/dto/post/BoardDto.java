package wegrus.clubwebsite.dto.post;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import wegrus.clubwebsite.entity.post.Board;

@Getter
@NoArgsConstructor
public class BoardDto {

    private Long boardId;
    private String boardName;
    private String boardCategoryName;

    @Builder
    public BoardDto(Board board){
        this.boardId = board.getId();
        this.boardName = board.getName();
        this.boardCategoryName = board.getBoardCategory().getName();
    }
}
