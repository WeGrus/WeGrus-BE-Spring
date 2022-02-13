package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class PostImageNotFoundException extends BusinessException{
    public PostImageNotFoundException() {
        super(ErrorCode.POST_IMAGE_NOT_FOUND);
    }
}
