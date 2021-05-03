package com.n26.model;

import com.n26.util.ApplicationUtil;
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

        public Statistics() {
                this.sum = ApplicationUtil.get2ScaledBigDecimal();
                this.avg = ApplicationUtil.get2ScaledBigDecimal();
                this.min = ApplicationUtil.get2ScaledBigDecimal();
                this.max = ApplicationUtil.get2ScaledBigDecimal();
                this.count = BigInteger.ZERO;
        }

        public void minOf(BigDecimal value) {
                this.min = this.min.equals(ApplicationUtil.get2ScaledBigDecimal()) ? value :this.min.min(value);
        }

        public void maxOf(BigDecimal value) {
                this.max = this.max.equals(ApplicationUtil.get2ScaledBigDecimal()) ? value : this.max.max(value);
        }

        public void add(BigDecimal value) {
                this.sum = this.sum.add(value);
                this.count = this.count.add(BigInteger.ONE);
                this.avg = this.sum.divide(BigDecimal.valueOf(count.longValue()), BigDecimal.ROUND_HALF_UP);
        }

        public String getSum() {
                return this.sum.toString();
        }
        public String getMin() {
                return this.min.toString();
        }
        public String getMax() {
                return this.max.toString();
        }
        public String getAvg() {
                return this.avg.toString();
        }
        public BigInteger getCount() {
                return this.count;
        }
}
