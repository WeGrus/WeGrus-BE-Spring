package wegrus.clubwebsite.dto.board;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@ApiModel(description = "댓글 등록 요청 데이터 모델")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReplyCreateRequest {

    @ApiModelProperty(value = "게시물 id", example = "1", required = true)
    @NotBlank(message = "게시물 id는 필수입니다.")
    private Long boardId;

    @ApiModelProperty(value = "댓글 내용", example = "댓글 내용입니다.", required = true)
    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String content;
}
