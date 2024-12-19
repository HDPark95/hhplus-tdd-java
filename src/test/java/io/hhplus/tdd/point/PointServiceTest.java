package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class PointServiceTest {

    private final UserPointTable userPointTable = Mockito.mock(UserPointTable.class);
    private final PointHistoryTable pointHistoryTable = Mockito.mock(PointHistoryTable.class);
    private final PointService pointService = new PointServiceV1Impl(userPointTable, pointHistoryTable);

    @Test
    public void 포인트_충전_성공(){
        //given
        long userId = 1L;
        long amount = 1000L;

        Mockito.when(userPointTable.selectById(userId)).thenReturn(new UserPoint(userId, 0, System.currentTimeMillis()));
        Mockito.when(userPointTable.insertOrUpdate(userId, amount)).thenReturn(new UserPoint(userId, amount, System.currentTimeMillis()));
        //when
        UserPoint userPoint = pointService.charge(userId, amount);

        //then
        Assertions.assertEquals(1000L, userPoint.point());
    }

    @Test
    public void 충전포인트를_더했을때_사용자의_보유_포인트가_10000000을_초과하면_포인트_충전에_실패한다(){
        //given
        long userId = 1L;
        long limitedAmount = 10000000L;

        Mockito.when(userPointTable.selectById(userId)).thenReturn(new UserPoint(userId, limitedAmount, System.currentTimeMillis()));

        //when then
        Assertions.assertThrows(TotalPointLimitExceededException.class, () -> {
            pointService.charge(userId, 1);
        });
    }
    @Test
    public void 포인트_사용_성공(){
        //given
        long userId = 1L;
        long amount = 1000L;

        Mockito.when(userPointTable.selectById(userId)).thenReturn(new UserPoint(userId, 1000, System.currentTimeMillis()));
        Mockito.when(userPointTable.insertOrUpdate(userId, 0)).thenReturn(new UserPoint(userId, 0, System.currentTimeMillis()));

        //when
        UserPoint userPoint = pointService.use(userId, amount);

        //then
        Assertions.assertEquals(0L, userPoint.point());
    }

    
    @Test
    public void 사용_포인트를_더했을때_사용자의_보유_포인트가_부족한_경우_실패한다(){
        //given
        long userId = 1L;
        long amount = 0L;

        Mockito.when(userPointTable.selectById(userId)).thenReturn(new UserPoint(userId, amount, System.currentTimeMillis()));

        //when then
        Assertions.assertThrows(InsufficientPointBalanceException.class, () -> {
            pointService.use(userId, amount + 1);
        });
    }
}