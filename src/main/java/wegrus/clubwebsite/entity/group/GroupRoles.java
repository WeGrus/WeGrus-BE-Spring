package wegrus.clubwebsite.entity.group;

import lombok.Getter;

@Getter
public enum GroupRoles {
    
    APPLICANT("가입 신청자"), 
    MEMBER("회원"), 
    EXECUTIVE("임원"), 
    PRESIDENT("회장");
    
    private final String value;

    GroupRoles(String value) {
        this.value = value;
    }
}
