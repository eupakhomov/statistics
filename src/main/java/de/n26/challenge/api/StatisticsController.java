package de.n26.challenge.api;

import de.n26.challenge.business.StatisticsBuffer;
import de.n26.challenge.util.AmountConverter;
import de.n26.challenge.util.CurrentTimeFactory;
import de.n26.challenge.util.DefaultUriBuilder;
import de.n26.challenge.util.UriBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.net.URI;

/**
 * Statistics controller to expose API.
 *
 * A {@link #updateStatistics(Transaction transaction) updateStatistics} method
 * called every time a transaction is made to add transaction data to statistics.
 * If transaction is older than statistics period it is skipped and HTTP status
 * 204 with empty body is returned.
 * If transaction is in the future it is skipped and HTTP status
 * 400 with empty body is returned.
 * If statistics updates successfully based on transaction data
 * HTTP status 201 with empty body is returned.
 *
 * Time boundaries:
 * if current time is 12:00:00.SSS then statistics for all
 * transactions from 11:59:01.000 to 12:00:00.999 will be
 * returned.
 *
 * A {@link #getStatistics() getStatistics} method
 * returns the statistic for a period.
 *
 * @author <a href=mailto:eugene.pakhomov@ubitricity.com>Eugene Pakhomov</a>
 */
@RestController
public class StatisticsController {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsController.class);

    private final int period;
    private final StatisticsBuffer<Statistics> statisticsBuffer;
    private final CurrentTimeFactory currentTimeFactory;
    private final UriBuilder uriBuilder;

    @Autowired
    public StatisticsController(StatisticsBuffer<Statistics> statisticsBuffer,
                                CurrentTimeFactory currentTimeFactory,
                                @Value("${de.n26.challenge.period}") int period) {
        this(statisticsBuffer, currentTimeFactory, period, new DefaultUriBuilder());
    }

    public StatisticsController(StatisticsBuffer<Statistics> statisticsBuffer,
                                CurrentTimeFactory currentTimeFactory,
                                int period,
                                UriBuilder uriBuilder
                                ) {
        this.statisticsBuffer = statisticsBuffer;
        this.currentTimeFactory = currentTimeFactory;
        this.period = period;
        this.uriBuilder = uriBuilder;
    }

    @RequestMapping(path = "/statistics", method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    Statistics getStatistics() {

        Statistics statistics = statisticsBuffer.calculate(currentTimeFactory.now());

        logger.debug("Statistics calculated: {}", statistics);

        return statistics;
    }

    @RequestMapping(path = "/transactions", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> updateStatistics(@RequestBody @Valid Transaction transaction) {

        logger.debug("Transaction received: {}", transaction);

        long timestamp = currentTimeFactory.now();
        long transactionTimeSec = transaction.getTimestamp() / 1000;

        // Transaction in the future - probably clock synchronization issue
        // Assuming application system clock is ok return 'Bad Request' HTTP status
        // Also return 'Bad Request' HTTP status in case if transaction parameters validation fails
        if(transactionTimeSec > timestamp || !isValidTransaction(transaction)) {
            logger.error("Transaction data is not valid: {}", transaction);

            return ResponseEntity.badRequest().build();
        }

        // Outdated transaction
        if(timestamp - transactionTimeSec >= period) {
            return ResponseEntity.noContent().build();
        }

        long convertedAmount = 0L;

        // Check for precision lost and return 'Bad Request' HTTP status in case value is to
        // big to process
        // In case we need to handle big values decimal part could be stored as a separate long
        try {
            convertedAmount = AmountConverter.toLongValue(transaction.getAmount());
        } catch (IllegalArgumentException ex) {
            logger.error("Error while updating statistics", ex);

            return ResponseEntity.badRequest().build();
        }

        statisticsBuffer.add(
                convertedAmount,
                transactionTimeSec,
                timestamp
        );

        URI location = uriBuilder.getUri();
        return ResponseEntity.created(location).build();
    }

    private boolean isValidTransaction(Transaction transaction) {
        // Timestamp is validated by Spring, double types we have to check manually
        return transaction.getAmount() > 0;
    }
}
