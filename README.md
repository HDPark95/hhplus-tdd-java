## 동시성 제어 방식에 대한 분석 및 보고서

### 포인트 서비스 요구사항

주어진 요구사항 및 정책은 아래와 같습니다. 

- PATCH  `/point/{id}/charge` : 포인트 충전
    1. 충전 금액은 0보다 커야 한다. 
    2. 총 포인트는 10,000,000을 초과할 수 없다.
    3. 1회 충전 금액은 1,000,000을 초과할 수 없다. 
- PATCH `/point/{id}/use` : 포인트 사용
    1. 사용 금액은 0보다 커야한다.
    2. 가지고 있는 포인트보다 많은 포인트를 사용할 수 없다.
    3. 1회 사용 금액은 1,000,000을 초과할 수 없다.
- GET `/point/{id}` : 포인트 조회
- GET `/point/{id}/histories` : 포인트 이력 조회
- 동시에 여러 건의 포인트 충전, 이용 요청이 들어올 경우 순차적으로 처리되어야 합니다.

```java
@Component
public class UserPointTable {

    private final Map<Long, UserPoint> table = new HashMap<>();

    public UserPoint selectById(Long id) {
        throttle(200); //조회에는 0.2초 지연
        return table.getOrDefault(id, UserPoint.empty(id));
    }

    public UserPoint insertOrUpdate(long id, long amount) {
        throttle(300); //upsert에는 0.3초 지연
        UserPoint userPoint = new UserPoint(id, amount, System.currentTimeMillis());
        table.put(id, userPoint);
        return userPoint;
    }

    private void throttle(long millis) {
        try {
            TimeUnit.MILLISECONDS.sleep((long) (Math.random() * millis));
        } catch (InterruptedException ignored) {

        }
    }
}
```

- 기본적으로 조회에는 0.2초 지연, Upsert에는 0.3초 지연이 존재합니다.

### 동시성 문제 정의
멀티스레드 환경에서 동일한 UserPoint에 여러 스레드가 동시에 접근할 경우, 데이터 불일치와 Race Condition이 발생할 수 있습니다.

예시:
충전 작업과 사용 작업이 순서 없이 실행될 경우, 정책 위반 및 데이터 손실 발생 가능.
동일한 사용자에 대해 여러 충전 요청이 들어오면 일부 충전 내역이 누락될 수 있음.

```java
@Service
@RequiredArgsConstructor
public class PointServiceV1Impl implements PointService{

    private static final Logger log = LoggerFactory.getLogger(PointServiceV2Impl.class);
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
}
```

- 테스트 코드

```java
@SpringBootTest
public class UserPointIntegrationTest {
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
    
    @Test
    public void 동시에_충전_테스트() throws InterruptedException, ExecutionException {
        //given
        long userId = 1L;
        long amount = 1000L;

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        List<ChargeJob> chargeJobs = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            chargeJobs.add(new ChargeJob(userId, amount));
        }

        List<Future<UserPoint>> futures = executorService.invokeAll(chargeJobs);
				Assertions.assertEquals(10000L, pointService.getPoint(userId).point());
    }
}
```

테스트 결과

![image](https://github.com/user-attachments/assets/26adbd9d-cedb-49e7-bd34-d9bbbc34110b)

![image](https://github.com/user-attachments/assets/adfda85f-42b2-4206-a16c-5f7f22b50fe2)


동시성 제어를 하지 않아, 쓰레드간 경쟁 상황이 발생했고, 마지막 결과 값이 예상한 값과 다른 것을 볼 수 있습니다.

## 동시성 제어 방식

### syncronized를 이용한 동시성 제어

- 자바에서 제공하는 동기화 메커니즘입니다. syncronized를 사용하여 임계영역을 정의하고, 한번에 하나의 스레드만 해당 영역이 실행되도록 합니다.
- 메서드 수준과, 블록 수준에서 동기화를 적용할 수 있습니다.
- **단점**:
    1. 모든 요청이 동일한 임계영역에서 대기해야 하므로, 병렬 처리가 불가능.
    2. 특정 사용자 기준으로만 락을 적용하는 요구사항을 만족시키기 어려움.
    3. 공정성을 보장하지 않으며, 중단 기능이 없음.
    

### `ConcurrentHashMap` 과 `ReentrantLock` 을 사용하여 동시성 제어

- 왜 동시성 컬렉션인 ConcurrentHashMap를 사용했을까?
자바의 기본 컬렉션은 **멀티스레드 환경에서 안전하지 않습니다**.
따라서 멀티스레드 환경에서 안전하려면 syncronized, Lock 등을 사용해야합니다. 
 따라서 멀티스레드 환경에서 안전하게 사용하려면 `synchronized`, `Lock` 등을 사용해야 합니다. **ConcurrentHashMap**은 내부적으로 **원자적 연산**과 **멀티스레드 안전성**을 제공합니다.
- 왜 ReentrantLock을 사용했을까?
    
    Lock 인터페이스는 동시성 프로그래밍에서 쓰이는 안전한 임계 영역을 구현한는 데 사용됩니다.
    
    ReentrantLock은 공정모드를 제공합니다. 
    
    공정 모드란, 락을 요청한 순서대로 스레드가 락을 획득할 수 있게 하는 것을 의미합니다.

  구현 로직

```java
@Override
    public UserPoint charge(long id, long amount) throws RuntimeException {
        Lock lock = lockMap.computeIfAbsent(id, k -> new ReentrantLock(true));
        lock.lock(); //락 획득
        try {
            log.info("포인트 충전 요청 - 유저 ID: {}, 충전 금액: {}, 쓰레드 ID: {}", id, amount, Thread.currentThread().getId());
            UserPoint currentPoint = userPointTable.selectById(id);

            UserPoint updatedPoint = userPointTable.insertOrUpdate(id, currentPoint.charge(amount));
            log.info("포인트 충전 완료 - 유저 ID: {}, 충전 금액: {}, 현재 포인트: {}, 쓰레드 ID: {}", id, amount, updatedPoint.point(), Thread.currentThread().getId());
            pointHistoryTable.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());
            return updatedPoint;
        }finally {
            lock.unlock(); //락 반납
        }
    }=
```

테스트 결과

![image](https://github.com/user-attachments/assets/7bc1ec60-f43d-477d-bc2e-9575b0787efa)


위와 같은 설계를 통해 포인트 충전 및 사용 로직의 동시성 문제를 성공적으로 해결하였습니다.
