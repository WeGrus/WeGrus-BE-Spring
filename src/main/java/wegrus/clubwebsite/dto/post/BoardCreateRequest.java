package wegrus.clubwebsite.dto.post;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.validation.constraints.NotNull;

@Getter
@AllArgsConstructor
public class BoardCreateRequest {

    @ApiModelProperty(value = "게시판 카테고리 id", example = "1", required = true)
    @NotNull(message = "게시판 카테고리 id는 필수입니다.")
    private Long boardCategoryId;

    @ApiModelProperty(value = "게시판 이름", example = "신규", required = true)
    @NotNull(message = "게시판 이름은 필수입니다.")
    private String boardName;
}
