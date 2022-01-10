package wegrus.clubwebsite.dto.member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import wegrus.clubwebsite.entity.Member;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MemberAndJwtDto {

    private Member member;
    private String accessToken;
    private String refreshToken;
}
