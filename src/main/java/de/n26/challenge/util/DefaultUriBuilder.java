package de.n26.challenge.util;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

/**
 * Helper class to isolate request URI getting in REST controller.
 *
 * @author <a href=mailto:eugene.pakhomov@ubitricity.com>Eugene Pakhomov</a>
 */
public class DefaultUriBuilder implements UriBuilder {

    @Override
    public URI getUri() {
        return ServletUriComponentsBuilder
                .fromCurrentRequest()
                .build()
                .toUri();
    }
}
