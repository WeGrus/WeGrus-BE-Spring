package wegrus.clubwebsite.exception;

import wegrus.clubwebsite.dto.error.ErrorCode;

public class BoardCategoryNotFoundException extends BusinessException{
    public BoardCategoryNotFoundException() {
        super(ErrorCode.BOARD_CATEGORY_NOT_FOUND);
    }
}
