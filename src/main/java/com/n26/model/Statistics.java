package com.n26.model;

import lombok.Data;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.BigInteger;

@Setter
@ToString
public class Statistics {
        private BigDecimal sum;
        private BigDecimal avg;
        private BigDecimal max;
        private BigDecimal min;
        private BigInteger count;
}
