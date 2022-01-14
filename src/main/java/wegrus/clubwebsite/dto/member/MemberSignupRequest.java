package wegrus.clubwebsite.dto.member;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import wegrus.clubwebsite.entity.member.MemberAcademicStatus;
import wegrus.clubwebsite.entity.member.MemberGrade;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@ApiModel(description = "회원가입 요청 데이터 모델")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberSignupRequest {

    @ApiModelProperty(value = "이메일", example = "12161542@inha.edu", required = true)
    @Pattern(regexp = "^[0-9]{8}@(inha.edu|inha.ac.kr)$", message = "인하대학교 이메일 형식만 가능합니다.")
    private String email;

    @ApiModelProperty(value = "카카오 회원 번호", example = "123456789", required = true)
    @NotNull(message = "카카오 회원 번호는 필수입니다.")
    private Long kakaoId;

    @ApiModelProperty(value = "회원 실명", example = "홍길동", required = true)
    @NotBlank(message = "회원 실명은 필수입니다.")
    private String name;

    @ApiModelProperty(value = "회원 학과", example = "컴퓨터공학과", required = true)
    @NotBlank(message = "회원 학과는 필수입니다.")
    private String department;

    @ApiModelProperty(value = "회원 연락처", example = "010-1234-5678", required = true)
    @Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}$", message = "연락처는 010-1234-5678 형식으로 입력해주세요.")
    private String phone;

    @ApiModelProperty(value = "회원 학적 상태", example = "ATTENDING", required = true)
    @NotNull(message = "회원 학적 상태는 필수입니다.")
    private MemberAcademicStatus academicStatus;

    @ApiModelProperty(value = "회원 학년", example = "FRESHMAN", required = true)
    @NotNull(message = "회원 학년은 필수입니다.")
    private MemberGrade grade;
}
