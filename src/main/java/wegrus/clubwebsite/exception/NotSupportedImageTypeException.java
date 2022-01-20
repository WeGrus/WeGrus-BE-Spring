package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class NotSupportedImageTypeException extends BusinessException {

    public NotSupportedImageTypeException() {
        super(ErrorCode.NOT_SUPPORTED_IMAGE_TYPE);
    }
}
