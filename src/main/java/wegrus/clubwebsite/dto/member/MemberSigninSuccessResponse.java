package wegrus.clubwebsite.dto.member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MemberSigninSuccessResponse {

    private String status;
    private MemberDto member;
    private String accessToken;
}
