package io.hhplus.tdd.point;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UseValidatorTest {

    @Test
    void _1보다_적은_값을_충전하면_실패() {
        long amount = 0L;
        assertThrows(UseAmountTooLowException.class, () -> {
            UseValidator.validate(amount);
        });
    }

    @Test
    void _1000000을_초과하는_값을_충전하면_실패() {
        long amount = 1000001L;
        assertThrows(SingleUseAmountLimitExceededException.class, () -> {
            UseValidator.validate(amount);
        });
    }
}