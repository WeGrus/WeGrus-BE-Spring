package wegrus.clubwebsite.dto.member;

import lombok.Getter;

@Getter
public enum MemberRoleSearchType {
    ALL("ROLE_GUEST"),
    MEMBER("ROLE_MEMBER"),
    EXECUTIVE("ROLE_CLUB_EXECUTIVE"),
    PRESIDENT("ROLE_CLUB_PRESIDENT")
    ;

    private final String roleName;

    MemberRoleSearchType(String roleName) {
        this.roleName = roleName;
    }
}
