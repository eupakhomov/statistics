package de.n26.challenge.business;

import de.n26.challenge.api.Statistics;
import de.n26.challenge.util.AmountConverter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Collector to collect statistics data in appropriate format for API.
 *
 * @author <a href=mailto:eugene.pakhomov@ubitricity.com>Eugene Pakhomov</a>
 */
@Component
public class DefaultStatisticsCollector implements StatisticsCollector<Statistics> {

    /**
     * Converts aggregated statistic data to the {@link Statistics} type.
     *
     * Here {@link BigDecimal} used for precise results.
     * In case performance has priority over precision double type
     * might be used directly.
     *
     * @param sum sum of transaction value from statistics buffer
     * @param count total number of transactions from statistics buffer
     * @param min single lowest transaction value from statistics buffer
     * @param max single highest transaction value from statistics buffer
     * @return Statistics container
     */
    @Override
    public Statistics collectStatistics(long sum, long count, long min, long max) {
        BigDecimal sumConverted = AmountConverter.toBigDecimalValue(sum);
        BigDecimal numberConverted = BigDecimal.valueOf(count);

        return Statistics.build()
                .sum(sumConverted.doubleValue())
                .avg(!numberConverted.equals(BigDecimal.ZERO)
                        ? sumConverted
                            .divide(numberConverted, BigDecimal.ROUND_HALF_EVEN)
                                .doubleValue()
                        : BigDecimal.ZERO.doubleValue())
                .count(count)
                .max(AmountConverter.toBigDecimalValue(max).doubleValue())
                .min(AmountConverter.toBigDecimalValue(min).doubleValue());
    }

    /**
     * Creates empty Statistics container.
     *
     * @return empty Statistics container
     */
    @Override
    public Statistics emptyStatistics() {
        return Statistics.EMPTY_STATISTICS;
    }
}
