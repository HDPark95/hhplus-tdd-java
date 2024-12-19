package io.hhplus.tdd.point;

public class SingleUseAmountLimitExceededException extends RuntimeException {
    public SingleUseAmountLimitExceededException(String message) {
        super(message);
    }

    public SingleUseAmountLimitExceededException() {
        super("1회 사용 금액은 1,000,000 포인트를 초과할 수 없습니다.");
    }
}
