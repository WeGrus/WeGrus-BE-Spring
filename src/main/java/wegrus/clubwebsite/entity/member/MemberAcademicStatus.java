package wegrus.clubwebsite.entity.member;

import lombok.Getter;

@Getter
public enum MemberAcademicStatus {

    ATTENDING("재학"),
    ABSENCE("휴학"),
    GRADUATED("졸업"),
    ETC("기타");

    private final String value;

    MemberAcademicStatus(String value) {
        this.value = value;
    }
}
