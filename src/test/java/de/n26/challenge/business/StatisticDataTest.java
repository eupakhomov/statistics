package de.n26.challenge.business;

import de.n26.challenge.api.Statistics;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Test {@link StatisticData}
 *
 * @author <a href=mailto:eugene.pakhomov@ubitricity.com>Eugene Pakhomov</a>
 */
public class StatisticDataTest {

    @Test
    public void reset() throws Exception {
        StatisticData statisticData = new StatisticData(60);
        statisticData.add(0, 10L, 1500077407L);

        statisticData.reset(0);

        assertThat(statisticData.getAmount(0), is(0L));
        assertThat(statisticData.getCount(0), is(0L));
        assertThat(statisticData.getMax(0), is(0L));
        assertThat(statisticData.getMin(0), is(0L));
        assertThat(statisticData.getTimestamp(0), is(0L));
    }

    @Test
    public void add() throws Exception {
        StatisticData statisticData = new StatisticData(60);
        statisticData.add(15, 10L, 1500077407L);
        statisticData.add(15, 15L, 1500077408L);
        statisticData.add(16, 20L, 1500077409L);

        assertThat(statisticData.getAmount(15), is(25L));
        assertThat(statisticData.getCount(15), is(2L));
        assertThat(statisticData.getMax(15), is(15L));
        assertThat(statisticData.getMin(15), is(10L));
        assertThat(statisticData.getTimestamp(15), is(1500077408L));

        assertThat(statisticData.getAmount(16), is(20L));
        assertThat(statisticData.getCount(16), is(1L));
        assertThat(statisticData.getMax(16), is(20L));
        assertThat(statisticData.getMin(16), is(20L));
        assertThat(statisticData.getTimestamp(16), is(1500077409L));
    }

    @Test
    public void collect() throws Exception {
        StatisticData statisticData = new StatisticData(60);
        statisticData.add(15, 10L, 1500077407L);
        statisticData.add(15, 15L, 1500077408L);
        statisticData.add(16, 20L, 1500077409L);

        Statistics statistics = statisticData.collect(new DefaultStatisticsCollector());
        Statistics statistics_expected = Statistics.build()
                .avg(0.15)
                .count(3)
                .max(0.2)
                .min(0.1)
                .sum(0.45);

        assertThat(statistics, is(statistics_expected));
    }

    @Test
    public void size() throws Exception {
        StatisticData statisticData = new StatisticData(60);
        assertThat(statisticData.size(), is(60));
    }

}
