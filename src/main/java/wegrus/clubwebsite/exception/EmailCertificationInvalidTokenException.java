package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class EmailCertificationInvalidTokenException extends BusinessException {

    public EmailCertificationInvalidTokenException() {
        super(ErrorCode.EMAIL_CERTIFICATION_TOKEN_INVALID);
    }
}
