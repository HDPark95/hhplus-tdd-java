package io.hhplus.tdd.point;

public class SingleChargeAmountLimitExceededException extends RuntimeException {
    public SingleChargeAmountLimitExceededException(String message) {
        super(message);
    }

    public SingleChargeAmountLimitExceededException() {
        super("1회 충전 금액은 1,000,000 포인트를 초과할 수 없습니다.");
    }
}
