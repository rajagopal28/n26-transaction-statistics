package com.n26.model;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class TransactionVO {

    @NotNull
    private String amount;

    @NotNull
    private String timestamp;
    public BigDecimal getDecimalAmount() {
        return new BigDecimal(this.amount);
    }
}
