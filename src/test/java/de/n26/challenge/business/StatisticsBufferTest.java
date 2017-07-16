package de.n26.challenge.business;

import de.n26.challenge.api.Statistics;
import de.n26.challenge.util.AmountConverter;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Test {@link StatisticsBuffer}
 *
 * @author <a href=mailto:eugene.pakhomov@ubitricity.com>Eugene Pakhomov</a>
 */
public class StatisticsBufferTest {

    @Test
    public void addWithinPeriod() throws Exception {
        StatisticsBuffer<Statistics> buffer = new StatisticsBuffer<>(60, new DefaultStatisticsCollector());

        buffer.add(1500L, timestampSecFor(10, 56),  timestampSecFor(10, 57));
        buffer.add(3000L, timestampSecFor(10, 56),  timestampSecFor(10, 58));
        buffer.add(3000L, timestampSecFor(10, 58),  timestampSecFor(10, 58));
        buffer.add(5000L, timestampSecFor(10, 59),  timestampSecFor(10, 59));
        buffer.add(7000L, timestampSecFor(10, 59),  timestampSecFor(10, 59));

        Statistics statistic_in_period = buffer.calculate(timestampSecFor(11, 01));
        Statistics statistic_part_period = buffer.calculate(timestampSecFor(11, 58));
        Statistics statistic_no_period = buffer.calculate(timestampSecFor(12, 0));

        Statistics statistic_in_period_expected = Statistics.build()
                .avg(39.0)
                .count(5)
                .max(70.0)
                .min(15.0)
                .sum(195.0);

        Statistics statistic_part_period_expected = Statistics.build()
                .avg(60.0)
                .count(2)
                .max(70.0)
                .min(50.0)
                .sum(120.0);

        Statistics statistic_no_period_expected = Statistics.EMPTY_STATISTICS;

        assertThat(statistic_in_period, is(statistic_in_period_expected));
        assertThat(statistic_part_period, is(statistic_part_period_expected));
        assertThat(statistic_no_period, is(statistic_no_period_expected));
    }

    @Test
    public void addCrossingPeriodBoundaries() throws Exception {
        StatisticsBuffer<Statistics> buffer = new StatisticsBuffer<>(60, new DefaultStatisticsCollector());

        buffer.add(1500L, timestampSecFor(10, 56),  timestampSecFor(10, 57));
        buffer.add(2000L, timestampSecFor(10, 58),  timestampSecFor(10, 58));
        buffer.add(2500L, timestampSecFor(11, 02),  timestampSecFor(11, 02));
        buffer.add(5000L, timestampSecFor(11, 02),  timestampSecFor(11, 03));
        buffer.add(6000L, timestampSecFor(11, 03),  timestampSecFor(11, 04));

        Statistics statistic_in_period = buffer.calculate(timestampSecFor(11, 07));
        Statistics statistic_part_period = buffer.calculate(timestampSecFor(11, 59));
        Statistics statistic_no_period = buffer.calculate(timestampSecFor(12, 05));

        Statistics statistic_in_period_expected = Statistics.build()
                .avg(34.0)
                .count(5)
                .max(60.0)
                .min(15.0)
                .sum(170.0);

        Statistics statistic_part_period_expected = Statistics.build()
                .avg(45.0)
                .count(3)
                .max(60.0)
                .min(25.0)
                .sum(135.0);

        Statistics statistic_no_period_expected = Statistics.EMPTY_STATISTICS;

        assertThat(statistic_in_period, is(statistic_in_period_expected));
        assertThat(statistic_part_period, is(statistic_part_period_expected));
        assertThat(statistic_no_period, is(statistic_no_period_expected));
    }


    @Test
    public void addWithTimeDicrepancy() throws Exception {
        StatisticsBuffer<Statistics> buffer = new StatisticsBuffer<>(60, new DefaultStatisticsCollector());

        buffer.add(1000L, timestampSecFor(10, 56),  timestampSecFor(10, 57));
        buffer.add(2000L, timestampSecFor(10, 58),  timestampSecFor(10, 58));
        buffer.add(5000L, timestampSecFor(11, 02),  timestampSecFor(11, 03));
        buffer.add(6000L, timestampSecFor(11, 03),  timestampSecFor(11, 04));
        buffer.add(4000L, timestampSecFor(10, 59),  timestampSecFor(11, 05));

        Statistics statistic_in_period = buffer.calculate(timestampSecFor(11, 07));
        Statistics statistic_part_period = buffer.calculate(timestampSecFor(11, 58));
        Statistics statistic_no_period = buffer.calculate(timestampSecFor(12, 05));

        Statistics statistic_in_period_expected = Statistics.build()
                .avg(36.0)
                .count(5)
                .max(60.0)
                .min(10.0)
                .sum(180.0);

        Statistics statistic_part_period_expected = Statistics.build()
                .avg(50.0)
                .count(3)
                .max(60.0)
                .min(40.0)
                .sum(150.0);

        Statistics statistic_no_period_expected = Statistics.EMPTY_STATISTICS;

        assertThat(statistic_in_period, is(statistic_in_period_expected));
        assertThat(statistic_part_period, is(statistic_part_period_expected));
        assertThat(statistic_no_period, is(statistic_no_period_expected));
    }

    @Test
    public void addSeveralCycles() {
        int cycles = 10;
        int period = 60;
        Random random = new Random();

        StatisticsBuffer<Statistics> buffer = new StatisticsBuffer<>(period, new DefaultStatisticsCollector());

        // First run several cycles
        for(int i = 0; i < cycles; i++) {
            long sum = 0;

            for(int j = 0; j < period; j++) {
                int n = random.ints(1, 0, 11)
                        .findFirst().getAsInt();

                for(int k = 0; k < n; k++) {
                    int delayMin = (i > 3 && k % 3 == 0)
                            ? random.ints(1, 0, 5)
                                .findFirst().getAsInt()
                            : 0;
                    int delaySec = (j > 20 && k % 2 == 0)
                            ? random.ints(1, 0, 20)
                            .findFirst().getAsInt()
                            : 0;
                    long amount = random.ints(1, 1000, 100000)
                            .findFirst().getAsInt();

                    if(delayMin == 0) {
                        sum += amount;
                    }

                    buffer.add(amount,
                            timestampSecFor(i + 1 - delayMin, j - delaySec),
                            timestampSecFor(i + 1, j));
                }

            }

            Statistics statistic_in_cycle = buffer.calculate(timestampSecFor(i + 1, 59));
            assertThat(statistic_in_cycle.getSum(), is(AmountConverter.toBigDecimalValue(sum).doubleValue()));
        }

        // Then check standard set
        int min = cycles + 2;
        buffer.add(1000L, timestampSecFor(min, 56),  timestampSecFor(min, 57));
        buffer.add(2000L, timestampSecFor(min, 58),  timestampSecFor(min, 58));
        buffer.add(5000L, timestampSecFor(min + 1, 02),  timestampSecFor(min + 1, 03));
        buffer.add(6000L, timestampSecFor(min + 1, 03),  timestampSecFor(min + 1, 04));
        buffer.add(4000L, timestampSecFor(min, 59),  timestampSecFor(min + 1, 05));

        Statistics statistic_in_period = buffer.calculate(timestampSecFor(min + 1, 07));
        Statistics statistic_part_period = buffer.calculate(timestampSecFor(min + 1, 58));
        Statistics statistic_no_period = buffer.calculate(timestampSecFor(min + 2, 05));

        Statistics statistic_in_period_expected = Statistics.build()
                .avg(36.0)
                .count(5)
                .max(60.0)
                .min(10.0)
                .sum(180.0);

        Statistics statistic_part_period_expected = Statistics.build()
                .avg(50.0)
                .count(3)
                .max(60.0)
                .min(40.0)
                .sum(150.0);

        Statistics statistic_no_period_expected = Statistics.EMPTY_STATISTICS;

        assertThat(statistic_in_period, is(statistic_in_period_expected));
        assertThat(statistic_part_period, is(statistic_part_period_expected));
        assertThat(statistic_no_period, is(statistic_no_period_expected));
    }

    @Test
    public void addWithConcurrencyWithHighContention() throws Exception {
        StatisticsBuffer<Statistics> buffer = new StatisticsBuffer<>(60, new DefaultStatisticsCollector());
        int writingThreadsNumber = 1000;

        final int min = 10;
        final Random random = new Random();
        final CountDownLatch startSignal = new CountDownLatch(1);
        final CountDownLatch doneSignal = new CountDownLatch(writingThreadsNumber);

        for (int i = 0; i < writingThreadsNumber; i++) {
            Thread thread = new Thread(
                    () -> {
                        try {
                            startSignal.await();
                            int sec = random.ints(1, 0, 60)
                                    .findFirst().getAsInt();
                            buffer.add(1000L, timestampSecFor(min, sec),  timestampSecFor(min, sec));
                        } catch (Exception e) {
                            fail(e.getMessage());
                        } finally {
                            doneSignal.countDown();
                        }
                    }
            );
            thread.start();
        }

        startSignal.countDown();
        doneSignal.await();

        Statistics statistic_in_period = buffer.calculate(timestampSecFor(min, 59));
        Statistics statistic_no_period = buffer.calculate(timestampSecFor(min + 2, 05));

        Statistics statistic_in_period_expected = Statistics.build()
                .avg(10.0)
                .count(writingThreadsNumber)
                .max(10.0)
                .min(10.0)
                .sum(10.0 * writingThreadsNumber);

        Statistics statistic_no_period_expected = Statistics.EMPTY_STATISTICS;

        assertThat(statistic_in_period, is(statistic_in_period_expected));
        assertThat(statistic_no_period, is(statistic_no_period_expected));
    }

    @Test
    public void calculate() throws Exception {
        StatisticsBuffer<Statistics> buffer = new StatisticsBuffer<>(60, new DefaultStatisticsCollector());
        Statistics statistic_empty = buffer.calculate(timestampSecFor(0, 0));
        assertThat(statistic_empty, is(Statistics.EMPTY_STATISTICS));
    }

    private long timestampSecFor(int min, int sec) {
        LocalDateTime now = LocalDateTime.now(ZoneId.ofOffset("", ZoneOffset.UTC));
        return now.withMinute(min).withSecond(sec).toEpochSecond(ZoneOffset.UTC);
    }
}
