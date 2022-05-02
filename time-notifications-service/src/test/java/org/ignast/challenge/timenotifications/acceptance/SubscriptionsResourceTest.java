package org.ignast.challenge.timenotifications.acceptance;

import static java.lang.String.format;
import static org.assertj.core.api.Assertions.assertThat;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.springframework.hateoas.MediaTypes.HAL_JSON;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import java.io.IOException;
import lombok.val;
import okhttp3.mockwebserver.MockWebServer;
import org.ignast.challenge.timenotifications.testutil.api.traversor.HateoasTraversor;
import org.json.JSONException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public final class SubscriptionsResourceTest {

    @LocalServerPort
    private int port;

    @Autowired
    private HateoasTraversor.Factory traversors;

    private static MockWebServer mockServer;

    @BeforeAll
    static void setUp() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockServer.shutdown();
    }

    @Test
    public void shouldCreateSubscription() throws JSONException, InterruptedException {
        val url = format("http://localhost:%d/", mockServer.getPort());
        val every4Secs = every4secAtLocalhost(mockServer.getPort());

        val result = traversors
            .startAt(root())
            .hop(f -> f.post("subscriptions:create", every4Secs))
            .perform();

        assertThat(result.getStatusCode()).isEqualTo(CREATED);
        assertThat(result.getHeaders().getContentType()).isEqualTo(HAL_JSON);
        assertEquals(every4Secs, result.getBody(), false);

        val recordedRequest = mockServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getHeader("Content-Type")).isEqualTo("application/json");
        assertThat(recordedRequest.getRequestUrl().toString()).isEqualTo(url);
        val body = recordedRequest.getBody().readUtf8();
        assertThat(body).startsWith("{\"time\":\"");
        assertThat(body).endsWith("[UTC]\"}");
    }

    @Test
    public void shouldRescheduleSubscription() throws JSONException {
        val every4Secs = every4secAtLocalhost(8081);

        val result = traversors
            .startAt(root())
            .hop(f -> f.post("subscriptions:create", every4Secs))
            .hop(f -> f.put("self", every4Secs))
            .perform();

        assertThat(result.getStatusCode()).isEqualTo(OK);
        assertEquals(every4Secs, result.getBody(), false);
    }

    @Test
    public void shouldRemoveSubscription() throws JSONException {
        val every4Secs = every4secAtLocalhost(8081);

        val result = traversors
            .startAt(root())
            .hop(f -> f.post("subscriptions:create", every4Secs))
            .hop(f -> f.delete("self"))
            .perform();

        assertThat(result.getStatusCode()).isEqualTo(OK);
    }

    @Test
    public void shouldNotCreateSubscriptionForDisallowedFrequencies() throws JSONException {
        val every3Secs = every3secAtLocalhost();

        val result = traversors
            .startAt(root())
            .hop(f -> f.post("subscriptions:create", every3Secs))
            .perform();

        assertThat(result.getStatusCode()).isEqualTo(BAD_REQUEST);
    }

    private static final String every4secAtLocalhost(final int port) {
        return format(
            """
                {
                    "subscriptionUri":"http://localhost:%d",
                    "frequency":{
                        "amount":4,
                        "timeUnit":"second"
                    }
                }""",
            port
        );
    }

    private static final String every3secAtLocalhost() {
        return """
                {
                    "subscriptionUri":"http://localhost:8081",
                    "frequency":{
                        "amount":3,
                        "timeUnit":"second"
                    }
                }""";
    }

    private String root() {
        return "http://localhost:" + port;
    }

    @TestConfiguration
    static class AcceptanceConfiguration {

        @Bean
        MediaType appMediaType() {
            return HAL_JSON;
        }
    }
}
