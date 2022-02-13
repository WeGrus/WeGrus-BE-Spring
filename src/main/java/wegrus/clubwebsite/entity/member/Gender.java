package wegrus.clubwebsite.entity.member;

import lombok.Getter;

@Getter
public enum Gender {

    MAN("남"),
    WOMAN("여");

    private final String value;

    Gender(String value) {
        this.value = value;
    }
}
