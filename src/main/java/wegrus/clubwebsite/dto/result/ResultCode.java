package wegrus.clubwebsite.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    // Member
    SIGNUP_SUCCESS(200, "M100", "회원가입에 성공하였습니다."),
    LOGIN_SUCCESS(200, "M101", "로그인에 성공하였습니다."),
    LOGOUT_SUCCESS(200, "M102", "로그아웃에 성공하였습니다."),

    // Verification
    REQUEST_VERIFY_SUCCESS(200, "V100", "인증 키 검증 요청에 성공하였습니다."),
    VERIFY_EMAIL_SUCCESS(200, "V101", "이메일 인증에 성공하였습니다."),
    ;

    private int status;
    private final String code;
    private final String message;
}