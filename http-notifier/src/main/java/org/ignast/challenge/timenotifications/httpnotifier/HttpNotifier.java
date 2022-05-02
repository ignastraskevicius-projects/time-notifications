package org.ignast.challenge.timenotifications.httpnotifier;

import java.net.URI;
import java.time.ZonedDateTime;
import org.ignast.challenge.timenotifications.domain.NotificationSender;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestClientException;
import org.springframework.web.reactive.function.client.WebClient;

@Repository
public class HttpNotifier implements NotificationSender {

    private final WebClient webClient;

    public HttpNotifier(final WebClient.Builder builder) {
        webClient = builder.build();
    }

    @Override
    public void send(final URI location, final ZonedDateTime time) {
        fireAndForget(location, time);
    }

    private void fireAndForget(final URI location, final ZonedDateTime time) {
        webClient
            .post()
            .uri(location)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(asJson(time))
            .retrieve()
            .bodyToMono(String.class)
            .subscribe();
    }

    private static final String asJson(final ZonedDateTime time) {
        return String.format("{\"time\":\"%s\"}", time.toString());
    }
}
