package wegrus.clubwebsite.dto.member;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;
import lombok.NoArgsConstructor;
import wegrus.clubwebsite.entity.group.GroupRoles;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class GroupDto {

    private Long id;
    private String name;
    private String  role;
    private LocalDateTime joinDate;

    @JsonIgnore
    private Long memberId;

    @QueryProjection
    public GroupDto(Long id, String name, GroupRoles role, LocalDateTime joinDate, Long memberId) {
        this.id = id;
        this.name = name;
        this.role = role.getValue();
        this.joinDate = joinDate;
        this.memberId = memberId;
    }
}
