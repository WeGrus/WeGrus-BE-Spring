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
    MULTIPARTFILE_CONVERT_FAIL(400, "C010", "MultipartFile을 File로 변환하는 데 실패하였습니다."),

    // Board
    BOARD_NOT_FOUND(400, "B000", "존재하지 않는 게시물입니다."),
    BOARD_MEMBER_NOT_MATCH(400, "B001", "게시물의 작성자와 일치하지 않습니다."),
    REPLY_NOT_FOUND(400, "B002", "존재하지 않는 댓글입니다."),
    REPLY_MEMBER_NOT_MATCH(400, "B003", "댓글의 작성자와 일치하지 않습니다."),
    POST_LIKE_ALREADY_EXIST(400, "B004", "이미 존재하는 게시물 추천입니다."),
    POST_LIKE_NOT_FOUND(400, "B005", "게시물 추천이 존재하지 않습니다."),
    COMMENT_LIKE_ALREADY_EXIST(400, "B006", "이미 존재하는 댓글 추천입니다."),
    COMMENT_LIKE_NOT_FOUND(400, "B007", "댓글 추천이 존재하지 않습니다."),

    // Member
    MEMBER_NOT_FOUND(400, "M000", "존재하지 않는 회원입니다."),
    USERID_ALREADY_EXIST(400, "M001", "이미 존재하는 아이디입니다."),
    EMAIL_ALREADY_EXIST(400, "M002", "이미 존재하는 이메일입니다."),
    INVALID_EMAIL(400, "M003", "인하대학교 이메일 형식만 가능합니다."),
    MEMBER_ROLE_NOT_FOUND(400, "M004", "존재하지 않는 회원 등급입니다."),
    MEMBER_IMAGE_ALREADY_BASIC(400, "M005", "회원 이미지가 이미 기본 이미지입니다."),
    EMAIL_CERTIFICATION_TOKEN_INVALID(400, "M006", "이메일 검증 토큰이 유효하지 않습니다."),
    MEMBER_ALREADY_HAS_ROLE(400, "M007", "이미 해당 권한을 가지고 있는 회원입니다."),
    MEMBER_CANNOT_RESIGN(400, "M008", "탈퇴를 할 수 없는 회원입니다."),
    CLUB_PRESIDENT_CANNOT_RESIGN(400, "M009", "동아리 회장은 회원 탈퇴를 할 수 없습니다."),
    RESIGNED_MEMBER_CANNOT_RESIGN(400, "M010", "이미 탈퇴한 회원은 회원 탈퇴를 할 수 없습니다."),
    BANNED_MEMBER_CANNOT_RESIGN(400, "M011", "이미 재가입 불가인 회원 탈퇴를 할 수 없습니다."),


    // File
    NOT_SUPPORTED_IMAGE_TYPE(400, "F000", "이미지 타입은 JPG, PNG, GIF만 지원합니다."),
    ;
    private int status;
    private final String code;
    private final String message;
}
