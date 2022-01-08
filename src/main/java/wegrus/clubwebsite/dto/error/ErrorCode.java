package wegrus.clubwebsite.dto.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // Common
    INTERNAL_SERVER_ERROR(500, "C000", "내부 서버 오류입니다."),
    INVALID_INPUT_VALUE(400, "C001", "유효하지 않은 입력입니다."),
    EXPIRED_ACCESS_TOKEN(401, "C002", "만료된 Access Token입니다."),
    EXPIRED_REFRESH_TOKEN(401, "C003", "만료된 Refresh Token입니다."),
    INVALID_JWT(401, "C004", "유효하지 않은 JWT입니다."),
    INVALID_AUTHORIZATION_HEADER(400, "C005", "유효하지 않은 인증 헤더입니다."),
    REFRESH_TOKEN_NOT_MATCH(400, "C006", "Refresh Token이 일치하지 않습니다."),
    SIGNATURE_NOT_MATCH(400, "C006", "JWT의 Signature이 일치하지 않습니다."),
    METHOD_NOT_ALLOWED(405, "C007", "허용되지 않은 HTTP method입니다."),
    INVALID_TYPE_VALUE(400, "C008", "입력 타입이 유효하지 않습니다."),
    INSUFFICIENT_AUTHORITY(403, "C009", "접근 권한이 부족합니다."),

    // Member
    MEMBER_NOT_FOUND(400, "M000", "존재하지 않는 회원입니다."),
    KAKAOID_ALREADY_EXIST(400, "M001", "이미 존재하는 카카오 회원 번호입니다."),
    EMAIL_ALREADY_EXIST(400, "M002", "이미 존재하는 이메일입니다."),

    // Verification
    EXPIRED_VERIFICATION_KEY(400, "V000", "만료된 인증 키입니다."),
    ;

    private int status;
    private final String code;
    private final String message;
}