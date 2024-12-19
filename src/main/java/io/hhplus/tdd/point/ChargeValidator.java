package io.hhplus.tdd.point;

public class ChargeValidator {
    public static void validate(long chargeAmount){
        if(chargeAmount <= 0){
            throw new ChargeAmountTooLowException("충전 금액은 0보다 커야 합니다.");
        }
        if(chargeAmount > 1000000){
            throw new SingleChargeAmountLimitExceededException("1회 충전 금액은 1,000,000 포인트를 초과할 수 없습니다.");
        }
    }
}
