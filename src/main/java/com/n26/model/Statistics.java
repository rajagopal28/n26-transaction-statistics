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

        /*
         *
         * Method computes new Minimum value based on the given amount value.
         * Checks on initial value to make sure ZERO is not considered as a Valid Minimum value
         *
         * Does not return value
         * */
        public void minOf(BigDecimal value) {
                this.min = this.min.equals(ApplicationUtil.get2ScaledBigDecimal()) ? value :this.min.min(value);
        }

        /*
         *
         * Method computes new Maximum value based on the given amount value.
         * Checks on initial value to make sure ZERO is not considered as a Valid Maximum value
         *
         * Does not return value
         * */
        public void maxOf(BigDecimal value) {
                this.max = this.max.equals(ApplicationUtil.get2ScaledBigDecimal()) ? value : this.max.max(value);
        }

        /*
        *
        * Method computes given transactional amount data to the existing statistic
        * The decimal point precisions is set to 2 places for the computed average
        *
        * Does not return value
        * */
        public void add(BigDecimal value) {
                this.sum = this.sum.add(value);
                this.count = this.count.add(BigInteger.ONE);
                this.avg = this.sum.divide(BigDecimal.valueOf(count.longValue()), BigDecimal.ROUND_HALF_UP);
        }

        /*
         *
         * Method to get the Sum value of the statistics
         *
         * @return the String value of the Sum
         * */
        public String getSum() {
                return this.sum.toString();
        }
        /*
         *
         * Method to get the Minimum value of the statistics
         *
         * @return the String value of the Minimum
         * */
        public String getMin() {
                return this.min.toString();
        }
        /*
         *
         * Method to get the Maximum value of the statistics
         *
         * @return the String value of the Maximum
         * */
        public String getMax() {
                return this.max.toString();
        }
        /*
         *
         * Method to get the Average value of the statistics
         *
         * @return the String value of the Average
         * */
        public String getAvg() {
                return this.avg.toString();
        }
        /*
         *
         * Method to get the Count value of the statistics
         *
         * @return the Integer value of the Sum
         * */
        public BigInteger getCount() {
                return this.count;
        }
}
