package de.n26.challenge.util;

import java.net.URI;

/**
 * Helper to isolate request URI getting in REST controller.
 *
 * @author <a href=mailto:eugene.pakhomov@ubitricity.com>Eugene Pakhomov</a>
 */
public interface UriBuilder {
    URI getUri();
}
