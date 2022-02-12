package wegrus.clubwebsite.entity.post;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Boards {
    NOTICE("공지사항"),               // 공지사항
    IXPLOIT("IXPLOIT"),             // 소모임 IXPLOIT
    IGDC("IGDC"),                   // 소모임 IGDC
    ALGORUS("ALGORUS"),             // 소모임 ALGORUS
    WEBGRUS("WEBGRUS"),             // 소모임 WEBGRUS
    STUDY("스터디"),                 // 스터디
    INFO("정보 공유"),               // 정보 공유 게시판
    PROJECT("프로젝트 모집"),         // 프로젝트 모집 게시판
    HOBBY("취미 톡방"),              // 취미 톡방 게시판
    Q_A("질문과 답변"),                // 질문/답변 게시판
    FREE("자유 게시판"),             // 자유 게시판
    SUGGEST("건의 사항")             // 건의 사항 게시판
    ;

    private String krName;

    Boards(String krName) {
        this.krName = krName;
    }

    public String getKrName() {
        return this.krName;
    }
}
