package de.n26.challenge.util;

/**
 * Intermediate interface to facilitate testing.
 *
 * @author <a href=mailto:eugene.pakhomov@ubitricity.com>Eugene Pakhomov</a>
 */
public interface CurrentTimeFactory {
    /*
     * @return current time in seconds
     */
    long now();
}
