package wegrus.clubwebsite.dto.member;

import lombok.Getter;

@Getter
public enum MemberSearchType {
    NAME("member_name"),
    STUDENT_ID("member_student_id"),
    DEPARTMENT("member_department"),
    PHONE("member_phone");

    private final String field;

    MemberSearchType(String field) {
        this.field = field;
    }
}
