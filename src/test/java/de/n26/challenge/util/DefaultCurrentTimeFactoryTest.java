package de.n26.challenge.util;

import org.junit.Test;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.*;

/**
 * Test {@link DefaultCurrentTimeFactory}
 *
 * @author <a href=mailto:eugene.pakhomov@ubitricity.com>Eugene Pakhomov</a>
 */
public class DefaultCurrentTimeFactoryTest {
    @Test
    public void now() throws Exception {
        DefaultCurrentTimeFactory factory = new DefaultCurrentTimeFactory();
        long now = System.currentTimeMillis() / 1000;
        long factoryNow = factory.now();

        assertThat("timestamp", factoryNow, greaterThanOrEqualTo(now));
    }

}
