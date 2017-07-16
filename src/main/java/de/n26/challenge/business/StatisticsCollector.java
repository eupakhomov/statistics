package de.n26.challenge.business;

/**
 * Collects statistics in appropriate format and creates empty statistics containers.
 *
 * @author <a href=mailto:eugene.pakhomov@ubitricity.com>Eugene Pakhomov</a>
 */
public interface StatisticsCollector<T> {

    /**
     * Converts aggregated statistic data to the type needed by superior logic.
     *
     * @param sum sum of transaction value from statistics buffer
     * @param count total number of transactions from statistics buffer
     * @param min single lowest transaction value from statistics buffer
     * @param max single highest transaction value from statistics buffer
     * @return aggregated statistic data converted to the type needed by superior logic
     */
    T collectStatistics(long sum, long count, long min, long max);

    /**
     * Creates empty statistic data container of type needed by superior logic.
     *
     * @return empty statistic data container of type needed by superior logic
     */
    T emptyStatistics();
}
