package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class ApplicantNotFoundException extends BusinessException {

    public ApplicantNotFoundException() {
        super(ErrorCode.APPLICANT_NOT_FOUND);
    }
}
