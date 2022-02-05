package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class BookmarkNotFoundException extends BusinessException{
    public BookmarkNotFoundException() {
        super(ErrorCode.BOOKMARK_NOT_FOUND);
    }
}
