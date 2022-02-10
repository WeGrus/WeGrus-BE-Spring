package wegrus.clubwebsite.dto.member;

import lombok.Getter;

@Getter
public enum MemberSortType {
    ID("member_id"),
    NAME("member_name"),
    STUDENT_ID("member_student_id"),
    DEPARTMENT("member_department"),
    PHONE("member_phone"),
    GENDER("member_gender"),
    ACADEMIC_STATUS("member_academic_status"),
    GRADE("member_grade");

    private final String field;

    MemberSortType(String field) {
        this.field = field;
    }
}
