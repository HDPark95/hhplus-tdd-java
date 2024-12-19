package io.hhplus.tdd.point;

import org.springframework.stereotype.Service;

import java.util.List;


public interface PointService {

    UserPoint charge(long id, long amount) throws RuntimeException;

    UserPoint getPoint(long id);

    List<PointHistory> getPointHistories(long id);

    UserPoint use(long id, long amount);
}
