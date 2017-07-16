package de.n26.challenge.util;

import java.math.BigDecimal;

/**
 * To convert double amount to long value and long to {@link BigDecimal} amount.
 * Assume we have no specific cases like Japanese Yen or Bahraini Dinar
 * - all amounts have 2 decimal fraction digits.
 * Otherwise the strategy pattern might be used here.
 *
 * @author <a href=mailto:eugene.pakhomov@ubitricity.com>Eugene Pakhomov</a>
 */
public class AmountConverter {
    private static final int SCALE = 2;
    private static final BigDecimal HUNDERD = BigDecimal.ONE.movePointRight(SCALE);
    private static final int MULTIPLIER = getMultiplier();

    /**
     * Converts amount represented as double to long.
     * Uses standard rounding to avoid issues with
     * truncating because of double precision.
     *
     * @param amount amount represented as double
     * @return converted amount
     */
    public static long toLongValue(double amount) {
        long result = Math.round(amount * MULTIPLIER);

        if(result == Long.MAX_VALUE) {
            throw new IllegalArgumentException("Double value is too big to convert to long");
        }

        return result;
    }

    /**
     * Converts amount represented as long to {@link BigDecimal}.
     *
     * @param amount amount represented as long
     * @return converted amount
     */
    public static BigDecimal toBigDecimalValue(long amount) {
        return BigDecimal.valueOf(amount).divide(HUNDERD);
    }

    private static int getMultiplier() {
        int result = 10;

        for(int i = 1; i < SCALE; i++) {
            result *= result;
        }

        return result;
    }
}
