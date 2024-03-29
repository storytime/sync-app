package com.github.storytime.other;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import static com.github.storytime.config.props.Constants.AUTHORIZATION_HEADER_NAME;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
public class Utils {

    private Utils() {
    }

    public static HttpHeaders createHeader(final String token) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION_HEADER_NAME, "Bearer " + token);
        headers.setContentType(APPLICATION_JSON);
        return headers;
    }
}
