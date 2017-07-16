package de.n26.challenge.api;

import de.n26.challenge.business.StatisticsBuffer;
import de.n26.challenge.util.CurrentTimeFactory;
import de.n26.challenge.util.UriBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.net.URISyntaxException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test {@link StatisticsController}
 *
 * @author <a href=mailto:eugene.pakhomov@ubitricity.com>Eugene Pakhomov</a>
 */
@RunWith(MockitoJUnitRunner.class)
public class StatisticsControllerTest {

    private static final String FAKE_LOCATION = "http://fake";

    @Mock
    private StatisticsBuffer<Statistics> statisticsBuffer;

    @Mock
    private CurrentTimeFactory currentTimeFactory;

    @Mock
    private UriBuilder uriBuilder;

    private StatisticsController statisticsController;

    private long nowSec = System.currentTimeMillis() / 1000;

    @Before
    public void setup() throws URISyntaxException {
        when(currentTimeFactory.now()).thenReturn(nowSec);
        when(uriBuilder.getUri()).thenReturn(new URI(FAKE_LOCATION));
        statisticsController = new StatisticsController(statisticsBuffer, currentTimeFactory, 60, uriBuilder);
    }

    @Test
    public void getStatistics() throws Exception {
        statisticsController.getStatistics();
        verify(statisticsBuffer).calculate(nowSec);
    }

    @Test
    public void updateStatisticsOutdatedTransaction() throws Exception {
        Transaction transaction = new Transaction();
        transaction.setTimestamp((nowSec - 61) * 1000);
        transaction.setAmount(0.5d);

        ResponseEntity<?> response = statisticsController.updateStatistics(transaction);
        assertThat(response.getStatusCode(), is(HttpStatus.NO_CONTENT));
    }

    @Test
    public void updateStatisticsTransactionInTheFuture() throws Exception {
        Transaction transaction = new Transaction();
        transaction.setTimestamp((nowSec + 1) * 1000);
        transaction.setAmount(0.5d);

        ResponseEntity<?> response = statisticsController.updateStatistics(transaction);
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void updateStatisticsTransactionWrongAmount() throws Exception {
        Transaction transaction = new Transaction();
        transaction.setTimestamp((nowSec - 55) * 1000);
        transaction.setAmount(0.0d);

        ResponseEntity<?> response = statisticsController.updateStatistics(transaction);
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));

        transaction.setAmount(-0.5d);
        response = statisticsController.updateStatistics(transaction);
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));

        transaction.setAmount(9223372036854775807.9d);
        response = statisticsController.updateStatistics(transaction);
        assertThat(response.getStatusCode(), is(HttpStatus.BAD_REQUEST));
    }

    @Test
    public void updateStatistics() throws Exception {
        Transaction transaction = new Transaction();
        long timestamp = (nowSec - 55) * 1000;

        transaction.setTimestamp(timestamp);
        transaction.setAmount(0.5d);

        ResponseEntity<?> response = statisticsController.updateStatistics(transaction);
        assertThat(response.getStatusCode(), is(HttpStatus.CREATED));
        assertThat(response.getHeaders().getLocation(), is(new URI(FAKE_LOCATION)));

        verify(statisticsBuffer).add(50L, timestamp / 1000, nowSec);
    }

}
