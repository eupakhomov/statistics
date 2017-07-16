package de.n26.challenge.util;

import org.junit.Test;

import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

/**
 * Test {@link AmountConverter}
 *
 * @author <a href=mailto:eugene.pakhomov@ubitricity.com>Eugene Pakhomov</a>
 */
public class AmountConverterTest {

    @Test
    public void toLongValue() throws Exception {
        double d = 1.99998;
        long l = AmountConverter.toLongValue(d);
        assertThat(l, is(200L));

        d = 1.00001;
        l = AmountConverter.toLongValue(d);
        assertThat(l, is(100L));

        d = 1.99;
        l = AmountConverter.toLongValue(d);
        assertThat(l, is(199L));

        d = 1.01;
        l = AmountConverter.toLongValue(d);
        assertThat(l, is(101L));
    }

    @Test
    public void toBigDecimalValue() throws Exception {
        long l = 201;
        BigDecimal bd = AmountConverter.toBigDecimalValue(l);
        assertThat(bd, is(new BigDecimal("2.01")));

        l = 199;
        bd = AmountConverter.toBigDecimalValue(l);
        assertThat(bd, is(new BigDecimal("1.99")));
    }

}
