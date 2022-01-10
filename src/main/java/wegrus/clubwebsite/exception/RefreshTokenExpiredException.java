package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class RefreshTokenExpiredException extends BusinessException {
    public RefreshTokenExpiredException(){
        super(ErrorCode.EXPIRED_REFRESH_TOKEN);
    }
}
