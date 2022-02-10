package wegrus.clubwebsite.dto.member;

import lombok.Getter;
import lombok.NoArgsConstructor;
import wegrus.clubwebsite.entity.member.Member;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class RequestDto {

    private Long id;
    private String role;
    private LocalDateTime requestDate;
    private MemberDto member;

    public RequestDto(Long id, String role, LocalDateTime requestDate, Member member) {
        this.id = id;
        this.role = role;
        this.requestDate = requestDate;
        this.member = new MemberDto(member);
    }
}
