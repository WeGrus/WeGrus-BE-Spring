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
    INVALID_AUTHORIZATION_CODE(400, "C011", "유효하지 않은 인증 코드입니다."),

    // Post
    POST_NOT_FOUND(400, "B000", "존재하지 않는 게시물입니다."),
    POST_MEMBER_NOT_MATCH(400, "B001", "게시물의 작성자와 일치하지 않습니다."),
    REPLY_NOT_FOUND(400, "B002", "존재하지 않는 댓글입니다."),
    REPLY_MEMBER_NOT_MATCH(400, "B003", "댓글의 작성자와 일치하지 않습니다."),
    POST_LIKE_ALREADY_EXIST(400, "B004", "이미 존재하는 게시물 추천입니다."),
    POST_LIKE_NOT_FOUND(400, "B005", "게시물 추천이 존재하지 않습니다."),
    REPLY_LIKE_ALREADY_EXIST(400, "B006", "이미 존재하는 댓글 추천입니다."),
    REPLY_LIKE_NOT_FOUND(400, "B007", "댓글 추천이 존재하지 않습니다."),
    BOARD_NOT_FOUND(400, "B008", "존재하지 않는 게시판입니다."),
    BOARD_CATEGORY_NOT_FOUND(400, "B009", "존재하지 않는 게시판 종류입니다."),
    POST_LIST_NOT_FOUND(400, "B010", "존재하지 않는 게시물 목록입니다."),
    BOOKMARK_ALREADY_EXIST(400, "B011", "이미 존재하는 북마크입니다."),
    BOOKMARK_NOT_FOUND(400, "B012", "존재하지 않는 북마크입니다."),

    // Member
    MEMBER_NOT_FOUND(400, "M000", "존재하지 않는 회원입니다."),
    USERID_ALREADY_EXIST(400, "M001", "이미 존재하는 아이디입니다."),
    EMAIL_ALREADY_EXIST(400, "M002", "이미 존재하는 이메일입니다."),
    INVALID_EMAIL(400, "M003", "인하대학교 이메일 형식만 가능합니다."),
    MEMBER_ROLE_NOT_FOUND(400, "M004", "존재하지 않는 회원 권한입니다."),
    MEMBER_IMAGE_ALREADY_BASIC(400, "M005", "회원 이미지가 이미 기본 이미지입니다."),
    EMAIL_CERTIFICATION_TOKEN_INVALID(400, "M006", "이메일 검증 토큰이 유효하지 않습니다."),
    MEMBER_ALREADY_HAS_ROLE(400, "M007", "이미 해당 권한을 가지고 있는 회원입니다."),
    MEMBER_CANNOT_RESIGN(400, "M008", "탈퇴를 할 수 없는 회원입니다."),
    CLUB_PRESIDENT_CANNOT_RESIGN(400, "M009", "동아리 회장은 회원 탈퇴를 할 수 없습니다."),
    MEMBER_ALREADY_RESIGN(400, "M010", "이미 탈퇴한 회원은 회원 탈퇴를 할 수 없습니다."),
    MEMBER_ALREADY_BAN(400, "M011", "이미 재가입 불가인 회원 탈퇴를 할 수 없습니다."),
    CERTIFICATION_CODE_INVALID(400, "M012", "유효하지 않은 인증 코드입니다."),
    GROUP_PRESIDENT_CANNOT_RESIGN(400, "M013", "그룹 회장은 회원 탈퇴를 할 수 없습니다."),

    // Request
    REQUEST_NOT_FOUND(400, "R000", "존재하지 않는 권한 요청입니다."),
    ALREADY_HAVE_AUTHORITY(400, "R001", "이미 가지고 있는 권한입니다."),
    REQUEST_ALREADY_EXIST(400, "R002", "이미 해당 권한을 요청하였습니다."),
    CANNOT_REQUEST_AUTHORITY(400, "R003", "요청할 수 없는 권한입니다."),

    // Group
    GROUP_NOT_FOUND(400, "G000", "존재하지 않는 그룹입니다."),
    GROUP_MEMBER_ALREADY_EXIST(400, "G001", "이미 해당 그룹에 속한 회원입니다."),

    // Club management
    CANNOT_BAN_MEMBER(400, "CM000", "강제 탈퇴시킬 수 없는 회원입니다."),
    CANNOT_DELEGATE_MEMBER(400, "CM001", "동아리원이 아닌 회원에게 동아리 회장 권한을 위임할 수 없습니다."),

    // File
    NOT_SUPPORTED_IMAGE_TYPE(400, "F000", "이미지 타입은 JPG, PNG, GIF만 지원합니다."),
    ;
    private int status;
    private final String code;
    private final String message;
}
