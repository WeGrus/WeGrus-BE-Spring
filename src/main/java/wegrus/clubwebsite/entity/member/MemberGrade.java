package wegrus.clubwebsite.entity.member;

import lombok.Getter;

@Getter
public enum MemberGrade {
    FRESHMAN("1"),
    SOPHOMORE("2"),
    JUNIOR("3"),
    SENIOR("4"),
    ETC("5↑");

    private final String value;

    MemberGrade(String value) {
        this.value = value;
    }
}
