package io.hhplus.tdd.point;

public class ChargeAmountTooLowException extends RuntimeException {
    public ChargeAmountTooLowException(String message) {
        super(message);
    }

    public ChargeAmountTooLowException() {
        super("충전 금액은 0보다 커야 합니다.");
    }
}
