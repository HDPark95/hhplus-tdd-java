package io.hhplus.tdd.point;

public class InsufficientPointBalanceException extends RuntimeException {
    public InsufficientPointBalanceException(String message) {
        super(message);
    }

    public InsufficientPointBalanceException() {
        super("가지고 있는 포인트보다 많은 포인트는 사용할 수 없습니다.");
    }
}
