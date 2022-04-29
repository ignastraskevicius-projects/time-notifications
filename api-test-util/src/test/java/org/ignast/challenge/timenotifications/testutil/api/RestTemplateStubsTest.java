package org.ignast.challenge.timenotifications.testutil.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ignast.challenge.timenotifications.testutil.api.RestTemplateStubs.stubExchanging;
import static org.springframework.http.HttpEntity.EMPTY;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

public final class RestTemplateStubsTest {

    @ParameterizedTest
    @ValueSource(strings = { "response1", "response2" })
    public void shouldRespondWithStubbedMessage(final String stubbedResponse) {
        final val rest = stubExchanging(stubbedResponse);

        final val response = rest.exchange("any", GET, any(), String.class);

        assertThat(response.getBody()).isEqualTo(stubbedResponse);
    }

    @ParameterizedTest
    @ValueSource(strings = { "GET", "PUT", "POST", "DELETE" })
    public void shouldRespondForMethods(final String methodAsString) {
        final val method = HttpMethod.valueOf(methodAsString);
        final val rest = stubExchanging("stubbedResponse");

        final val response = rest.exchange("any", method, any(), String.class);

        assertThat(response.getStatusCode()).isEqualTo(OK);
        assertThat(response.getBody()).isEqualTo("stubbedResponse");
    }

    @ParameterizedTest
    @ValueSource(strings = { "uri2", "uri1" })
    public void shouldRespondForUris(final String uri) {
        final val rest = stubExchanging("stubbedResponse");

        final val response = rest.exchange(uri, GET, any(), String.class);

        assertThat(response.getBody()).isEqualTo("stubbedResponse");
    }

    private HttpEntity<?> any() {
        return EMPTY;
    }
}
