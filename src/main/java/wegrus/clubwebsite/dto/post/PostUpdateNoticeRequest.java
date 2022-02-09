package wegrus.clubwebsite.dto.post;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import wegrus.clubwebsite.entity.post.PostType;

import javax.validation.constraints.NotNull;

@ApiModel(description = "댓글 등록 요청 데이터 모델")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateNoticeRequest {
    @ApiModelProperty(value = "게시물 id", example = "1", required = true)
    @NotNull(message = "게시물 id는 필수입니다.")
    private Long postId;

    @ApiModelProperty(value = "공지사항 여부", example = "NORMAL", required = true)
    @NotNull(message = "공지사항 여부는 필수입니다.")
    private PostType type;
}
