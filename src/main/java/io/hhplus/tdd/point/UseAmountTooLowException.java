package io.hhplus.tdd.point;

public class UseAmountTooLowException extends RuntimeException {
    public UseAmountTooLowException(String message) {
        super(message);
    }

    public UseAmountTooLowException() {
        super("사용 금액은 0보다 커야 합니다.");
    }
}
