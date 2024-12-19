package io.hhplus.tdd.point;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@SpringBootTest
public class UserPointIntegrationTest {

    @Autowired
    private PointService pointService;

    private class UseJob implements Callable<UserPoint> {
        private long userId;
        private long amount;

        public UseJob(long userId, long amount) {
            this.userId = userId;
            this.amount = amount;
        }

        @Override
        public UserPoint call() {
            return pointService.use(userId, amount);
        }
    }
    private class ChargeJob implements Callable<UserPoint> {
        private long userId;
        private long amount;

        public ChargeJob(long userId, long amount) {
            this.userId = userId;
            this.amount = amount;
        }

        @Override
        public UserPoint call() {
            return pointService.charge(userId, amount);
        }
    }

    private class GetPointJob implements  Callable<UserPoint> {
        private long userId;

        public GetPointJob(long userId) {
            this.userId = userId;
        }

        @Override
        public UserPoint call() {
            return pointService.getPoint(userId);
        }
    }

    @Test
    public void 동시에_충전_테스트() throws InterruptedException, ExecutionException {
        //given
        long userId = 1L;
        long amount = 1000L;

        ExecutorService executorService = Executors.newFixedThreadPool(5);
        List<ChargeJob> chargeJobs = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            chargeJobs.add(new ChargeJob(userId, amount));
        }

        executorService.invokeAll(chargeJobs);
        Assertions.assertEquals(10000L, pointService.getPoint(userId).point());
    }

    @Test
    public void 동시에_사용_테스트() throws InterruptedException {
        long userId = 2L;
        long amount = 1000L;
        pointService.charge(userId, 100000L);

        ExecutorService executorService = Executors.newFixedThreadPool(5);
        List<UseJob> useJobs = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            useJobs.add(new UseJob(userId, amount));
        }

        executorService.invokeAll(useJobs);
        Assertions.assertEquals(0L,  pointService.getPoint(userId).point());
    }

    @Test
    public void 동시에_충전_사용_테스트() throws InterruptedException {
        long userId = 3L;
        long amount = 1000L;

        ExecutorService executorService = Executors.newFixedThreadPool(5);
        List<Callable<UserPoint>> jobs = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            jobs.add(new ChargeJob(userId, amount));
            jobs.add(new UseJob(userId, amount));
        }

        executorService.invokeAll(jobs);
        UserPoint userPoint = pointService.getPoint(userId);
        Assertions.assertEquals(0L, userPoint.point());
    }
}
