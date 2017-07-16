package de.n26.challenge.integration;

import de.n26.challenge.api.Statistics;
import de.n26.challenge.api.StatisticsController;
import de.n26.challenge.api.Transaction;
import de.n26.challenge.util.CurrentTimeFactory;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

/**
 * Integration test for {@link StatisticsController}
 *
 * @author <a href=mailto:eugene.pakhomov@ubitricity.com>Eugene Pakhomov</a>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StatisticsControllerIT {

    @MockBean
    private CurrentTimeFactory currentTimeFactory;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void tetsGetStatistics() {
        Statistics statistics = restTemplate.getForObject("/statistics", Statistics.class);
        assertThat(statistics, is(Statistics.EMPTY_STATISTICS));
    }

    @Test
    public void tetsUpdateStatistics() {
        when(currentTimeFactory.now()).thenReturn(timestampSecFor(10, 57));

        ResponseEntity<?> response = updateStatistics(timestampMilliFor(10, 56), 10.0);
        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));

        when(currentTimeFactory.now()).thenReturn(timestampSecFor(10, 58));

        response = updateStatistics(timestampMilliFor(10, 58), 20.0);
        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));

        when(currentTimeFactory.now()).thenReturn(timestampSecFor(11, 03));

        response = updateStatistics(timestampMilliFor(11, 02), 50.0);
        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));

        when(currentTimeFactory.now()).thenReturn(timestampSecFor(11, 04));

        response = updateStatistics(timestampMilliFor(11, 03), 60.0);
        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));

        when(currentTimeFactory.now()).thenReturn(timestampSecFor(11, 05));

        response = updateStatistics(timestampMilliFor(10, 59), 40.0);
        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));

        when(currentTimeFactory.now()).thenReturn(timestampSecFor(11, 07));

        Statistics statistic_in_period = restTemplate.getForObject("/statistics", Statistics.class);

        when(currentTimeFactory.now()).thenReturn(timestampSecFor(11, 58));

        Statistics statistic_part_period = restTemplate.getForObject("/statistics", Statistics.class);

        when(currentTimeFactory.now()).thenReturn(timestampSecFor(12, 05));

        Statistics statistic_no_period = restTemplate.getForObject("/statistics", Statistics.class);

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

        Assert.assertThat(statistic_in_period, CoreMatchers.is(statistic_in_period_expected));
        Assert.assertThat(statistic_part_period, CoreMatchers.is(statistic_part_period_expected));
        Assert.assertThat(statistic_no_period, CoreMatchers.is(statistic_no_period_expected));
    }

    @Test
    public void tetsUpdateStatisticsOutdated() {
        when(currentTimeFactory.now()).thenReturn(timestampSecFor(11, 57));

        ResponseEntity<?> response = updateStatistics(timestampMilliFor(10, 56), 10.0);
        assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT));
    }

    @Test
    public void tetsUpdateStatisticsInvalidTimestamp() {
        ResponseEntity<?> response = updateStatistics(-1L, 100.0);
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    private ResponseEntity<?> updateStatistics(long timestamp, double amount) {
        Transaction transaction = new Transaction();
        transaction.setTimestamp(timestamp);
        transaction.setAmount(amount);

        HttpEntity<Transaction> request = new HttpEntity<>(transaction);
        return restTemplate.exchange("/transactions", HttpMethod.POST, request, Void.class);
    }

    private long timestampSecFor(int min, int sec) {
        LocalDateTime now = LocalDateTime.now(ZoneId.ofOffset("", ZoneOffset.UTC));
        return now.withMinute(min).withSecond(sec).toEpochSecond(ZoneOffset.UTC);
    }

    private long timestampMilliFor(int min, int sec) {
        LocalDateTime now = LocalDateTime.now(ZoneId.ofOffset("", ZoneOffset.UTC));
        return now.withMinute(min).withSecond(sec).toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
