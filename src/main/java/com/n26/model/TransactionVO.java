package com.n26.model;

import com.n26.util.ApplicationUtil;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class TransactionVO {

    @NotNull
    private String amount;

    @NotNull
    private String timestamp;

    /*
     *
     * Method to get the BigDecimal value of the amount
     *
     * @return the String value of the amount
     * */
    public BigDecimal getDecimalAmount() {
        return ApplicationUtil.get2ScaledBigDecimal(this.amount);
    }
}
