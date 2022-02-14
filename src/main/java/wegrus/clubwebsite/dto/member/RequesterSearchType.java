package wegrus.clubwebsite.dto.member;

import lombok.Getter;

@Getter
public enum RequesterSearchType {
    NAME("member_name"),
    STUDENT_ID("member_student_id");

    private final String field;

    RequesterSearchType(String field) {
        this.field = field;
    }
}
