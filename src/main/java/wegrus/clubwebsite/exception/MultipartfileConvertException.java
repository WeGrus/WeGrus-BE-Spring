package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class MultipartfileConvertException extends BusinessException {

    public MultipartfileConvertException() {
        super(ErrorCode.MULTIPARTFILE_CONVERT_FAIL);
    }
}
