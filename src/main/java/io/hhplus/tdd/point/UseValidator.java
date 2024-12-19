package io.hhplus.tdd.point;

public class UseValidator {
    public static void validate(long amount) {
        if (amount <= 0) {
            throw new UseAmountTooLowException("사용 포인트가 0보다 작거나 같습니다.");
        }
        if (amount > 1000000) {
            throw new SingleUseAmountLimitExceededException("1회 사용 포인트는 1,000,000를 초과할 수 없습니다.");
        }
    }
}
