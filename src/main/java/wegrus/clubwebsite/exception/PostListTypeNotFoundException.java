package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class PostListTypeNotFoundException extends BusinessException {
    public PostListTypeNotFoundException() {
        super(ErrorCode.POST_LIST_NOT_FOUND);
    }
}
