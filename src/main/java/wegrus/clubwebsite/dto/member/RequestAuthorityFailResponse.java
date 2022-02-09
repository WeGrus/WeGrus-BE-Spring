package wegrus.clubwebsite.dto.member;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RequestAuthorityFailResponse extends RequestAuthorityResponse {

    private String reason;

    public RequestAuthorityFailResponse(String status, String reason) {
        super.setStatus(status);
        this.reason = reason;
    }
}
