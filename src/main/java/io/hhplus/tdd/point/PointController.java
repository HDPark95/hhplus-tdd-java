package io.hhplus.tdd.point;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/point")
@RequiredArgsConstructor
public class PointController {

    private static final Logger log = LoggerFactory.getLogger(PointController.class);
    private final PointService pointService;

    @GetMapping("{id}")
    public UserPoint point(
            @PathVariable long id
    ) {
        return pointService.getPoint(id);
    }

    @GetMapping("{id}/histories")
    public List<PointHistory> history(
            @PathVariable long id
    ) {
        return pointService.getPointHistories(id);
    }

    /**
     * 행동 분석
     * 1. 사용자 아이디와 충전 금액을 넘겨받는다.
     * 2. 사용자의 포인트를 충전 금액만큼 증가시킨다.
     * 3. 충전 이력을 저장한다.
     * 4. 사용자의 포인트를 반환한다.
     *
     * 실패케이스
     * 1. 충전 금액이 0보다 작거나 같은 경우
     * 2. 충전 금액이 1,000,000 포인트를 초과하는 경우
     * 3. 충전 금액을 더했을 때 사용자의 보유 포인트가 10,000,000 포인트를 초과하는 경우
     */
    @PatchMapping("{id}/charge")
    public UserPoint charge(
            @PathVariable long id,
            @RequestBody long amount
    ) {
        log.info("포인트 충전 요청 - 유저 ID: {}, 충전 금액: {}", id, amount);
        ChargeValidator.validate(amount);
        return pointService.charge(id, amount);
    }

    /**
     * 행동 분석
     * 1. 사용자 아이디와 사용 금액을 넘겨받는다.
     * 2. 사용자의 포인트를 사용 금액만큼 감소시킨다.
     * 3. 사용 이력을 저장한다.
     * 4. 사용자의 포인트를 반환한다.
     *
     * 실패 케이스
     * 1. 사용 금액이 0보다 작거나 같은 경우
     * 2. 사용 금액이 1,000,000 포인트를 초과하는 경우
     * 3. 사용 금액이 사용자의 보유 포인트를 초과하는 경우
     */
    @PatchMapping("{id}/use")
    public UserPoint use(
            @PathVariable long id,
            @RequestBody long amount
    ) {
        log.info("포인트 사용 요청 - 유저 ID: {}, 사용 금액: {}", id, amount);
        UseValidator.validate(amount);
        return pointService.use(id, amount);
    }
}
