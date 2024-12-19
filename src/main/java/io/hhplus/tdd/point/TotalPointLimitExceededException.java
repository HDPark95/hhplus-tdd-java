package io.hhplus.tdd.point;

public class TotalPointLimitExceededException extends RuntimeException {
    public TotalPointLimitExceededException(String message) {
        super(message);
    }

    public TotalPointLimitExceededException() {
        super("총 포인트는 10,000,000 포인트를 초과할 수 없습니다.");
    }
}
