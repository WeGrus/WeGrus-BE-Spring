package wegrus.clubwebsite.dto.member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class EmailCheckResponse {

    private String status;
    private String reason;
    private String verificationKey = "";

    public EmailCheckResponse(String status, String reason) {
        this.status = status;
        this.reason = reason;
    }
}
