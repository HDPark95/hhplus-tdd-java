package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PointServiceV1Impl implements PointService{

    private static final Logger log = LoggerFactory.getLogger(PointServiceV1Impl.class);
    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    @Override
    public UserPoint charge(long id, long amount) throws RuntimeException {
        log.info("포인트 충전 요청 - 유저 ID: {}, 충전 금액: {}, 쓰레드 ID: {}", id, amount, Thread.currentThread().getId());
        UserPoint currentPoint = userPointTable.selectById(id);

        UserPoint updatedPoint = userPointTable.insertOrUpdate(id, currentPoint.charge(amount));
        log.info("포인트 충전 완료 - 유저 ID: {}, 충전 금액: {}, 현재 포인트: {}, 쓰레드 ID: {}", id, amount, updatedPoint.point(), Thread.currentThread().getId());
        pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());
        return updatedPoint;
    }

    @Override
    public UserPoint getPoint(long id) {
        return userPointTable.selectById(id);
    }

    @Override
    public List<PointHistory> getPointHistories(long id) {
        return pointHistoryTable.selectAllByUserId(id);
    }

    @Override
    public UserPoint use(long id, long amount) {
        log.info("포인트 사용 요청 - 유저 ID: {}, 사용 금액: {}, 쓰레드 ID: {}", id, amount, Thread.currentThread().getId());
        UserPoint currentPoint = userPointTable.selectById(id);

        UserPoint updatedPoint = userPointTable.insertOrUpdate(id, currentPoint.use(amount));
        log.info("포인트 사용 완료 - 유저 ID: {}, 사용 금액: {}, 현재 포인트: {}, 쓰레드 ID: {}", id, amount, updatedPoint.point(), Thread.currentThread().getId());
        pointHistoryTable.insert(id, amount, TransactionType.USE, System.currentTimeMillis());
        return updatedPoint;
    }
}
