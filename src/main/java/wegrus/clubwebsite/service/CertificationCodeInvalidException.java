package wegrus.clubwebsite.service;

import wegrus.clubwebsite.dto.error.ErrorCode;
import wegrus.clubwebsite.exception.BusinessException;

public class CertificationCodeInvalidException extends BusinessException {

    public CertificationCodeInvalidException() {
        super(ErrorCode.CERTIFICATION_CODE_INVALID);
    }
}
