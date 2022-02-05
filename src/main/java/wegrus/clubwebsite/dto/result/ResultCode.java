package wegrus.clubwebsite.dto.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    // Member
    SIGNUP_SUCCESS(200, "M100", "회원가입에 성공하였습니다."),
    SIGNIN_SUCCESS(200, "M101", "로그인에 성공하였습니다."),
    SIGNOUT_SUCCESS(200, "M102", "로그아웃에 성공하였습니다."),
    REISSUE_SUCCESS(200, "M103", "토큰 재발급에 성공하였습니다."),
    NEED_TO_SIGNUP(200, "M104", "회원가입을 먼저 해주세요."),
    CHECK_EMAIL_SUCCESS(200, "M105", "이메일 검증에 성공하였습니다."),
    VALID_EMAIL(200, "M106", "사용 가능한 이메일입니다. 이메일에서 메일 인증을 완료해주세요."),
    GET_MEMBER_INFO_SUCCESS(200, "M107", "회원 정보 조회에 성공하였습니다."),
    UPDATE_MEMBER_INFO_SUCCESS(200, "M108", "회원 정보 수정에 성공하였습니다."),
    UPDATE_MEMBER_IMAGE_SUCCESS(200, "M109", "회원 이미지 변경에 성공하였습니다."),
    VALIDATE_EMAIL_SUCCESS(200, "M110", "이메일 인증 여부 확인에 성공하였습니다."),
    EMAIL_NOT_VERIFIED(200, "M111", "인증되지 않은 이메일입니다. 메일 인증을 먼저 해주세요."),
    EMAIL_IS_VERIFIED(200, "M112", "인증된 이메일입니다. 회원가입을 계속 진행해 주세요."),
    REQUEST_AUTHORITY_SUCCESS(200, "M113", "권한 요청에 성공하였습니다."),
    MEMBER_RESIGN_SUCCESS(200, "M114", "회원 탈퇴에 성공하였습니다."),
    NEED_TO_REJOIN(200, "M115", "탈퇴한 회원입니다. 회원 가입을 먼저 해주세요."),
    BANNED_USER(200, "M116", "강제 탈퇴된 회원입니다."),
    SEND_CERTIFICATION_CODE_SUCCESS(200, "M117", "회원의 이메일로 인증 코드 발송에 성공하였습니다."),

    // Post
    CREATE_POST_SUCCESS(200, "B100", "게시물 등록에 성공하였습니다."),
    UPDATE_POST_SUCCESS(200, "B101", "게시물 수정에 성공하였습니다."),
    DELETE_POST_SUCCESS(200, "B102", "게시물 삭제에 성공하였습니다."),
    VIEW_POST_SUCCESS(200, "B103", "게시물 조회에 성공하였습니다."),
    CREATE_REPLY_SUCCESS(200, "B104", "댓글 등록에 성공하였습니다."),
    DELETE_REPLY_SUCCESS(200, "B105", "댓글 삭제에 성공하였습니다."),
    CREATE_POST_LIKE_SUCCESS(200, "B106", "게시물 추천에 성공하였습니다."),
    DELETE_POST_LIKE_SUCCESS(200, "B107", "게시물 추천 해제에 성공하였습니다."),
    CREATE_REPLY_LIKE_SUCCESS(200, "B108", "댓글 추천에 성공하였습니다."),
    DELETE_REPLY_LIKE_SUCCESS(200, "B109", "댓글 추천 해제에 성공하였습니다."),
    VIEW_BOARD_SUCCESS(200, "B110", "게시판 목록 조회에 성공하였습니다."),
    CREATE_BOARD_SUCCESS(200, "B111", "게시판 추가에 성공하였습니다."),
    DELETE_BOARD_SUCCESS(200, "B112", "게시판 삭제에 성공하였습니다."),
    VIEW_POST_LIST_SUCCESS(200, "B113", "게시판 목록 조회에 성공하였습니다."),
    SEARCH_BY_TITLE_SUCCESS(200, "B114", "제목 검색에 성공하였습니다."),
    SEARCH_BY_WRITER_SUCCESS(200, "B115", "작성자 검색에 성공하였습니다."),
    SEARCH_BY_ALL_SUCCESS(200, "B116", "제목, 내용 검색에 성공하였습니다."),
    CREATE_BOOKMARK_SUCCESS(200, "B117", "북마크 등록에 성공하였습니다."),

    // Verification
    REQUEST_VERIFY_SUCCESS(200, "V100", "인증 키 검증 요청에 성공하였습니다."),
    VERIFY_EMAIL_SUCCESS(200, "V101", "이메일 인증에 성공하였습니다."),
    EXPIRED_VERIFICATION_KEY(200, "V102", "만료된 인증 키입니다."),
    ;

    private int status;
    private final String code;
    private final String message;
}
