package org.ignast.challenge.timenotifications.acceptance;

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

    @Test
    public void shouldCreateSubscription() {}

    @TestConfiguration
    static class AcceptanceConfiguration {

        @Bean
        MediaType appMediaType() {
            return MediaType.APPLICATION_JSON;
        }
    }
}
