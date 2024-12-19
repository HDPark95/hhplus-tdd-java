package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
public class PointServiceV2Impl implements PointService{

    private final Map<Long, Lock> lockMap = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(PointServiceV2Impl.class);
    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    @Override
    public UserPoint charge(long id, long amount) throws RuntimeException {
        Lock lock = lockMap.computeIfAbsent(id, k -> new ReentrantLock(true));
        lock.lock();
        try {
            log.info("포인트 충전 요청 - 유저 ID: {}, 충전 금액: {}, 쓰레드 ID: {}", id, amount, Thread.currentThread().getId());
            UserPoint currentPoint = userPointTable.selectById(id);

            UserPoint updatedPoint = userPointTable.insertOrUpdate(id, currentPoint.charge(amount));
            log.info("포인트 충전 완료 - 유저 ID: {}, 충전 금액: {}, 현재 포인트: {}, 쓰레드 ID: {}", id, amount, updatedPoint.point(), Thread.currentThread().getId());
            pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());
            return updatedPoint;
        }finally {
            lock.unlock();
        }
    }

    @Override
    public UserPoint getPoint(long id) {
        Lock lock = lockMap.computeIfAbsent(id, k -> new ReentrantLock(true));
        lock.lock();
        try{
            log.info("포인트 조회 요청 - 유저 ID: {}, 쓰레드 ID: {}", id, Thread.currentThread().getId());
            return userPointTable.selectById(id);
        }finally {
            lock.unlock();
        }
    }
    //공유자원을 UserPoint로 한정하여, 히스토리조회의 경우 lock을 걸지 않는다.
    @Override
    public List<PointHistory> getPointHistories(long id) {
        return pointHistoryTable.selectAllByUserId(id);
    }

    @Override
    public UserPoint use(long id, long amount) {
        Lock lock = lockMap.computeIfAbsent(id, k -> new ReentrantLock(true));
        lock.lock();
        try{
            log.info("포인트 사용 요청 - 유저 ID: {}, 사용 금액: {}, 쓰레드 ID: {}", id, amount, Thread.currentThread().getId());
            UserPoint currentPoint = userPointTable.selectById(id);

            UserPoint updatedPoint = userPointTable.insertOrUpdate(id, currentPoint.use(amount));
            log.info("포인트 사용 완료 - 유저 ID: {}, 사용 금액: {}, 현재 포인트: {}, 쓰레드 ID: {}", id, amount, updatedPoint.point(), Thread.currentThread().getId());
            pointHistoryTable.insert(id, amount, TransactionType.USE, System.currentTimeMillis());
            return updatedPoint;
        }finally {
            lock.unlock();
        }
    }
}
