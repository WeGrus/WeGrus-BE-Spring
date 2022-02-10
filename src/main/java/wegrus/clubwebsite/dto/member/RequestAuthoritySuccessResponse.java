package wegrus.clubwebsite.dto.member;

import lombok.Getter;
import lombok.NoArgsConstructor;
import wegrus.clubwebsite.entity.member.MemberRoles;

@Getter
@NoArgsConstructor
public class RequestAuthoritySuccessResponse extends RequestAuthorityResponse {

    private MemberRoles role;

    public RequestAuthoritySuccessResponse(String status, MemberRoles role) {
        super.setStatus(status);
        this.role = role;
    }
}
