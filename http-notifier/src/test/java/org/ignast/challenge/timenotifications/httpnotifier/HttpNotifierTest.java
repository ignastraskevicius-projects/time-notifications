package org.ignast.challenge.timenotifications.httpnotifier;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import lombok.val;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

final class HttpNotifierTest {

    private static final ZonedDateTime TOKYO_2022_01_01_00_00_01 = LocalDateTime
        .of(2022, Month.JANUARY, 1, 0, 0, 1, 0)
        .atZone(ZoneId.of("Asia/Tokyo"));
    private static final ZonedDateTime ANY_TIME = TOKYO_2022_01_01_00_00_01;

    private final HttpNotifier httpNotifier = new HttpNotifier(WebClient.builder());

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
    public void shouldNotifyTheProvidedUri() throws InterruptedException {
        mockServer.enqueue(new MockResponse());
        final String url = String.format("http://localhost:%d/", mockServer.getPort());
        final val uri = URI.create(url);

        httpNotifier.send(uri, TOKYO_2022_01_01_00_00_01);

        val recordedRequest = mockServer.takeRequest();
        assertThat(recordedRequest.getMethod()).isEqualTo("POST");
        assertThat(recordedRequest.getHeader("Content-Type")).isEqualTo("application/json");
        assertThat(recordedRequest.getRequestUrl().toString()).isEqualTo(url);
        assertThat(recordedRequest.getBody().readUtf8())
            .isEqualTo("{\"time\":\"2022-01-01T00:00:01+09:00[Asia/Tokyo]\"}");
    }

    @Test
    public void shouldIgnoreClientSideErrors() throws InterruptedException {
        mockServer.enqueue(new MockResponse().setResponseCode(400));
        final String url = String.format("http://localhost:%d/", mockServer.getPort());
        final val uri = URI.create(url);

        httpNotifier.send(uri, ANY_TIME);

        val recordedRequest = mockServer.takeRequest();
        assertThat(recordedRequest.getRequestUrl().toString()).isEqualTo(url);
    }

    @Test
    public void shouldIgnoreServerSideErrors() throws InterruptedException {
        mockServer.enqueue(new MockResponse().setResponseCode(500));
        final String url = String.format("http://localhost:%d/", mockServer.getPort());
        final val uri = URI.create(url);

        httpNotifier.send(uri, ANY_TIME);

        val recordedRequest = mockServer.takeRequest();
        assertThat(recordedRequest.getRequestUrl().toString()).isEqualTo(url);
    }

    @Test
    public void shouldUnresolvableHosts() throws InterruptedException {
        mockServer.enqueue(new MockResponse().setResponseCode(500));
        final String url = "http://unresolvableHost:8081/";
        final val uri = URI.create(url);

        httpNotifier.send(uri, ANY_TIME);
    }
}
