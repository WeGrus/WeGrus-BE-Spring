package wegrus.clubwebsite.dto.post;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import wegrus.clubwebsite.entity.post.PostType;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@ApiModel(description = "게시물 등록 요청 데이터 모델")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateRequest {

    @ApiModelProperty(value = "게시판 종류", example = "BOARD", required = true)
    @NotNull(message = "게시판 종류는 필수입니다.")
    private String boardName;

    @ApiModelProperty(value = "게시물 타입", example = "NOT_NOTICE", required = true)
    @NotBlank(message = "게시물 타입(공지사항 여부)은 필수입니다.")
    private PostType type;

    @ApiModelProperty(value = "게시물 제목", example = "게시판 제목 1", required = true)
    @NotBlank(message = "게시판 제목은 필수입니다.")
    private String title;

    @ApiModelProperty(value = "게시물 내용", example = "게시물 내용입니다.", required = true)
    @NotNull(message = "게시물 내용는 필수입니다.")
    private String content;

    @ApiModelProperty(value = "비밀글 여부", example = "false", required = true)
    private boolean secretFlag;

}
