package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class MemberImageAlreadyBasicException extends BusinessException {

    public MemberImageAlreadyBasicException() {
        super(ErrorCode.MEMBER_IMAGE_ALREADY_BASIC);
    }
}
