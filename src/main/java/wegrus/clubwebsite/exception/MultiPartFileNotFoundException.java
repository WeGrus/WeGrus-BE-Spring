package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class MultiPartFileNotFoundException extends BusinessException {
    public MultiPartFileNotFoundException() {
        super(ErrorCode.MULTIPARTFILE_NOT_FOUND);
    }
}
