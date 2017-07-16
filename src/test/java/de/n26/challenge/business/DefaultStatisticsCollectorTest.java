package de.n26.challenge.business;

import de.n26.challenge.api.Statistics;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Test {@link DefaultStatisticsCollector}
 *
 * @author <a href=mailto:eugene.pakhomov@ubitricity.com>Eugene Pakhomov</a>
 */
public class DefaultStatisticsCollectorTest {

    private DefaultStatisticsCollector collector = new DefaultStatisticsCollector();

    @Test
    public void collectStatistics() throws Exception {
        Statistics statistics = collector.collectStatistics(1005L, 50L, 10L, 101L);
        assertThat(statistics.getSum(), is(10.05d));
        assertThat(statistics.getAvg(), is(0.20d));
        assertThat(statistics.getMax(), is(1.01d));
        assertThat(statistics.getMin(), is(0.10d));
        assertThat(statistics.getCount(), is(50L));
    }

    @Test
    public void emptyStatistics() throws Exception {
        Statistics statistics = collector.emptyStatistics();
        assertThat(statistics, is(Statistics.EMPTY_STATISTICS));
    }

}
