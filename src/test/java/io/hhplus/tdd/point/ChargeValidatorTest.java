package io.hhplus.tdd.point;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChargeValidatorTest {

    @Test
    void _1보다_적은_금액이왔을때_오류발생() {
        // given
        long amount = 0;

        // when
        assertThrows(ChargeAmountTooLowException.class, () -> ChargeValidator.validate(amount));
    }

    @Test
    void _1000000보다_많은_금액이왔을때_오류발생() {
        // given
        long amount = 1000001;

        // when
        assertThrows(SingleChargeAmountLimitExceededException.class, () -> ChargeValidator.validate(amount));
    }

}