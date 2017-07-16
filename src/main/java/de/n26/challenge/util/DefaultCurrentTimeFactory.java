package de.n26.challenge.util;

import org.springframework.stereotype.Component;

/**
 * Default implementation of  {@link CurrentTimeFactory}.
 *
 * @author <a href=mailto:eugene.pakhomov@ubitricity.com>Eugene Pakhomov</a>
 */
@Component
public class DefaultCurrentTimeFactory implements CurrentTimeFactory {
    /*
     * @return current time in seconds
     */
    @Override
    public long now() {
        return System.currentTimeMillis() / 1000;
    }
}
