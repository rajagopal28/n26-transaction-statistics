package com.n26.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class TransactionVO {
    private String amount;
    private String timestamp;
    public BigDecimal getDecimalAmount() {
        return new BigDecimal(this.amount);
    }
}
