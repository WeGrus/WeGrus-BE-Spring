package wegrus.clubwebsite.dto.post;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@ApiModel(description = "게시물 수정 요청 데이터 모델")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateRequest {
    @ApiModelProperty(value = "게시물 id", example = "1", required = true)
    @NotNull(message = "게시물 id는 필수입니다.")
    private Long postId;

    @ApiModelProperty(value = "게시물 제목", example = "게시판 제목 1", required = true)
    @NotBlank(message = "게시판 제목은 필수입니다.")
    private String title;

    @ApiModelProperty(value = "게시물 내용", example = "게시물 내용입니다.", required = true)
    @NotNull(message = "게시물 내용는 필수입니다.")
    private String content;

    @ApiModelProperty(value = "비밀글 여부", example = "false", required = true)
    private boolean secretFlag;
}
