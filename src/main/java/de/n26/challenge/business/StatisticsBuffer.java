package de.n26.challenge.business;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Thread-safe bounded buffer to store statistic data with additional logic to
 * fit time boundaries. Implemented in circular fashion with per second data aggregation to allow constant
 * time and memory operations. Outdated data is cleaned on each invocation of public class methods.
 * Unlikely there will be a high contention ratio (roughly it must be more than 10 millions requests per second on
 * computer with Intel Core i3-3110M @ 2.40GHz processor for that) so synchronization with the
 * {@link StatisticsBuffer#statisticData} monitor is used.
 *
 * For more performant solution something lock-free like LMAX Disruptor with background copy-on-write and
 * aggregation might be used.
 *
 * @author <a href=mailto:eugene.pakhomov@ubitricity.com>Eugene Pakhomov</a>
 */
@Component
public class StatisticsBuffer<T> {

    private static final int NOT_INITIALIZED_CURSOR = -1;

    private final StatisticData statisticData;
    private final StatisticsCollector<T> collector;
    private long oldestTimestamp = 0L;
    private int oldestPosition = NOT_INITIALIZED_CURSOR;

    @Autowired
    public StatisticsBuffer(@Value("${de.n26.challenge.period}") int period, StatisticsCollector<T> collector) {
        this.statisticData = new StatisticData(period);
        this.collector = collector;
    }

    /**
     * Adds transaction data to the buffer.
     * Before addition outdated data is cleaned.
     * If transation is outdated or in the future - it will be skipped.
     *
     * @param transactionAmountLong amount represented as a long value
     * @param transactionTimeSec transaction time represented in seconds (truncated)
     * @param nowSec current time represented in seconds (truncated)
     */
    public void add(long transactionAmountLong, long transactionTimeSec, long nowSec) {

        // Skip transactions out of the period boundaries
        if(nowSec - transactionTimeSec >= statisticData.size() || transactionTimeSec > nowSec) {
            return;
        }

        int second = LocalDateTime
                .ofEpochSecond(transactionTimeSec, 0, ZoneOffset.UTC)
                .getSecond();

        synchronized(statisticData) {
            clearStale(nowSec);
            statisticData.add(second, transactionAmountLong, transactionTimeSec);
            setOldest(second, transactionTimeSec);
        }
    }

    /**
     * Return statistic data aggregated for period stored in the buffer.
     * Before calculation outdated data is cleaned.
     *
     * @param nowSec current time represented in seconds (truncated)
     * @return statistic data aggregated for period stored in the buffer
     */
    public T calculate(long nowSec) {
        synchronized(statisticData) {
            clearStale(nowSec);

            if(empty()) {
                return collector.emptyStatistics();
            } else {
                return statisticData.collect(collector);
            }
        }
    }

    private void setOldest(int second, long transactionTime) {
        if(empty() || transactionTime < oldestTimestamp) {
            updateCursor(second, transactionTime);
        }
    }

    private void updateCursor(int position, long timestamp) {
        oldestPosition = position;
        oldestTimestamp = timestamp;
    }

    private void clearStale(long nowSec) {
        int stale = getStale(nowSec);

        if(stale > 0) {
            clear(stale);
        }
    }

    private void clear(int n) {
        if(empty()) {
            return;
        }

        int period = statisticData.size();
        if(n == period) {
            // Complete reset
            reset();
        } else {
            // Clear stale and update cursor
            int k = oldestPosition;

            for (int i = 0; i < n; i++) {
                statisticData.reset((k + i) % period);
            }
            int moveCursor = (k + n) % period;

            while(statisticData.getTimestamp(moveCursor) == 0L) {
                moveCursor = (moveCursor + 1) % period;

                if(moveCursor == oldestPosition) {
                    markEmpty();
                    return;
                }
            }

            updateCursor(moveCursor, statisticData.getTimestamp(moveCursor));
        }
    }

    private int getStale(long now) {
        if(empty())
            return 0;

        int period = statisticData.size();
        long delta = now - oldestTimestamp;

        return delta < period ? 0 : checkStaleSize(delta - period + 1);
    }

    private int checkStaleSize(long stale) {
        int period = statisticData.size();
        // No precision loss as period is int
        return stale > period ? period : (int) stale;
    }

    private boolean empty() {
        return oldestPosition == NOT_INITIALIZED_CURSOR;
    }

    private void reset() {
        for (int i = 0; i < statisticData.size(); i++) {
            statisticData.reset(i);
        }

        markEmpty();
    }

    private void markEmpty() {
        oldestPosition = NOT_INITIALIZED_CURSOR;
    }
}
