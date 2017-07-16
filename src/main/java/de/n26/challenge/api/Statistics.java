package de.n26.challenge.api;

import java.io.Serializable;
import java.util.Objects;

/**
 * DTO to store statistics data for last N seconds.
 *
 * @author <a href=mailto:eugene.pakhomov@ubitricity.com>Eugene Pakhomov</a>
 */
public class Statistics implements Serializable {

    private static final long serialVersionUID = -5064565319477195400L;

    public static final Statistics EMPTY_STATISTICS = new Statistics();

    private double sum;
    private double avg;
    private double max;
    private double min;
    private long count;

    public double getSum() {
        return sum;
    }

    public void setSum(double sum) {
        this.sum = sum;
    }

    public double getAvg() {
        return avg;
    }

    public void setAvg(double avg) {
        this.avg = avg;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    // Fluent build
    public static Statistics build() {
        return new Statistics();
    }

    public Statistics sum(double sum) {
        this.sum = sum;
        return this;
    }

    public Statistics avg(double avg) {
        this.avg = avg;
        return this;
    }

    public Statistics max(double max) {
        this.max = max;
        return this;
    }

    public Statistics min(double min) {
        this.min = min;
        return this;
    }

    public Statistics count(long count) {
        this.count = count;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Statistics that = (Statistics) o;
        return Objects.equals(sum, that.sum) &&
                Objects.equals(avg, that.avg) &&
                Objects.equals(max, that.max) &&
                Objects.equals(min, that.min) &&
                Objects.equals(count, that.count);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sum, avg, max, min, count);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Statistics{");
        sb.append("sum=").append(sum);
        sb.append(", avg=").append(avg);
        sb.append(", max=").append(max);
        sb.append(", min=").append(min);
        sb.append(", count=").append(count);
        sb.append('}');
        return sb.toString();
    }
}
