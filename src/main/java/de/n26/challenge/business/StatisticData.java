package de.n26.challenge.business;

/**
 * Container to provide operations on statistics data.
 *
 * @author <a href=mailto:eugene.pakhomov@ubitricity.com>Eugene Pakhomov</a>
 */
public class StatisticData {
    private final int size;
    private final long[] amounts;
    private final long[] counts;
    private final long[] mins;
    private final long[] maxs;
    private final long[] timestamps;

    public StatisticData(int size) {
        this.size = size;
        amounts = new long[size];
        counts = new long[size];
        mins = new long[size];
        maxs = new long[size];
        timestamps = new long[size];
    }

    /**
     * Reset statistic data at given cursor position to zeros
     *
     * @param cursor cursor position
     */
    public void reset(int cursor) {

        assert cursor < size;

        amounts[cursor] = 0L;
        counts[cursor] = 0L;
        mins[cursor] = 0L;
        maxs[cursor] = 0L;
        timestamps[cursor] = 0L;
    }

    /**
     * Add transaction data to the certain position according to the cursor and save timestamp for it.
     *
     * @param cursor current cursor position
     * @param amount transaction amount
     * @param timestamp transaction timestamp
     */
    public void add(int cursor, long amount, long timestamp) {

        assert cursor < size;
        assert amount > 0L;
        assert timestamp > 0L;

        amounts[cursor] += amount;
        counts[cursor]++;

        // Assuming no zero amount transactions
        if(mins[cursor] == 0 || mins[cursor] > amount) {
            mins[cursor] = amount;
        }

        if(maxs[cursor] < amount) {
            maxs[cursor] = amount;
        }

        timestamps[cursor] = timestamp;
    }

    /**
     * Calculate aggregated statistic data and pass to collector to transform into type
     * demanded by superior logic.
     *
     * @param collector collector to accept aggregated data and transform it into needed type
     * @param <T> type to transform aggregated data into
     * @return aggregated and transformed into needed type statistic data
     */
    public <T> T collect(StatisticsCollector<T> collector) {

        assert collector != null;

        long sum = 0;
        long count = 0;
        long maximum = 0;
        long minimum = 0;

        for(int i = 0; i < size; i++) {
            sum += amounts[i];
            count += counts[i];

            // Assuming no zero amount transactions
            if((minimum == 0 || minimum > mins[i]) && mins[i] != 0) {
                minimum = mins[i];
            }

            if(maximum < maxs[i]) {
                maximum = maxs[i];
            }
        }

        return collector.collectStatistics(sum, count, minimum, maximum);
    }

    public int size() {
        return size;
    }

    public long getAmount(int cursor) {
        return amounts[cursor];
    }

    public long getCount(int cursor) {
        return counts[cursor];
    }

    public long getMin(int cursor) {
        return mins[cursor];
    }

    public long getMax(int cursor) {
        return maxs[cursor];
    }

    public long getTimestamp(int cursor) {
        return timestamps[cursor];
    }
}
