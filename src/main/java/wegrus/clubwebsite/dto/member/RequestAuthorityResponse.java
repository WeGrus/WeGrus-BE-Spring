package wegrus.clubwebsite.dto.member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import wegrus.clubwebsite.entity.member.MemberRoles;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RequestAuthorityResponse {

    private String status;
    private MemberRoles role;
}
