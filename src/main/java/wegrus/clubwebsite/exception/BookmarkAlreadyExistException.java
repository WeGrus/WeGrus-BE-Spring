package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class BookmarkAlreadyExistException extends BusinessException {
    public BookmarkAlreadyExistException() {
        super(ErrorCode.BOOKMARK_ALREADY_EXIST);
    }
}
