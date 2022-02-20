package wegrus.clubwebsite.entity.post;

public enum BoardCategories {
    NOTICE("공지사항"),            // 공지사항
    GROUP("소모임"),              // 소모임
    STUDY("스터디"),              // 스터디
    BOARD("게시판")                 // 게시판
    ;

    private String krName;

    BoardCategories(String krName) {
        this.krName = krName;
    }

    public String getKrName() {
        return this.krName;
    }
}
