package wegrus.clubwebsite.dto.member;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MemberInfoResponse {

    private String status;
    private Object info;

    public MemberInfoResponse(String status, MemberDto memberDto) {
        this.status = status;
        this.info = memberDto;
    }

    public MemberInfoResponse(String status, MemberSimpleDto memberSimpleDto) {
        this.status = status;
        this.info = memberSimpleDto;
    }
}
