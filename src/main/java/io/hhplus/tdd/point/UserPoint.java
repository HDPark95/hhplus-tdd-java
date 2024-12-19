package io.hhplus.tdd.point;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }

    public long charge(long amount) {
        if (point + amount > 10000000) throw new TotalPointLimitExceededException("총 포인트는 10,000,000 포인트를 초과할 수 없습니다.");
        return point + amount;
    }

    public long use(long amount) {
        if (point < amount) throw new InsufficientPointBalanceException("가지고 있는 포인트보다 많은 포인트는 사용할 수 없습니다.");
        return point - amount;
    }
}
