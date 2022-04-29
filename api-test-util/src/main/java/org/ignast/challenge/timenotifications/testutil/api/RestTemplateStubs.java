package org.ignast.challenge.timenotifications.testutil.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.ResponseEntity.status;

import lombok.val;
import org.mockito.ArgumentMatchers;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public final class RestTemplateStubs {

    private RestTemplateStubs() {}

    public static RestTemplate stubExchanging(final String response) {
        final val restTemplate = mock(RestTemplate.class);
        when(restTemplate.exchange(any(String.class), any(), any(), ArgumentMatchers.<Class<String>>any()))
            .thenReturn(ok(response));
        return restTemplate;
    }

    private static ResponseEntity<String> ok(final String response) {
        return status(OK).body(response);
    }
}
