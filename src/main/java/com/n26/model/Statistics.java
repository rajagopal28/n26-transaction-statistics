package com.n26.model;

import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
@ToString
public class Statistics {
        private BigDecimal sum;
        private BigDecimal avg;
        private BigDecimal max;
        private BigDecimal min;
        private BigInteger count;

        public Statistics() {
                this.sum = BigDecimal.ZERO;
                this.avg = BigDecimal.ZERO;
                this.min = BigDecimal.valueOf(Double.MAX_VALUE);
                this.max = BigDecimal.valueOf(Double.MIN_VALUE);
                this.count = BigInteger.ZERO;
        }

        public void minOf(BigDecimal value) {
                this.min = this.min.min(value);
        }

        public void maxOf(BigDecimal value) {
                this.max = this.max.max(value);
        }

        public void add(BigDecimal value) {
                this.sum = this.sum.add(value);
                this.count = this.count.add(BigInteger.ONE);
                this.avg = this.sum.divide(BigDecimal.valueOf(count.longValue()), 2);
        }
}
