package org.ignast.challenge.timenotifications.testutil.api.traversor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.ignast.challenge.timenotifications.testutil.api.traversor.HateoasLink.link;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.parseMediaType;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

@RestClientTest(HateoasTraversor.Factory.class)
public final class HateoasTraversorTest {

    private static final String ROOT_URI = "http://root";

    private static final MediaType APP_V1 = parseMediaType("application/app.specific.media.type-v1.hal+json");

    @Autowired
    private HateoasTraversor.Factory traversors;

    @Autowired
    private MockRestServiceServer server;

    @Test
    public void shouldNotBeCreatedWithNullRestTemplateBuilder() {
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> new HateoasTraversor.Factory(null, APPLICATION_JSON));
    }

    @Test
    public void shouldNotBeCreatedWithNullAppMediaType() {
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> new HateoasTraversor.Factory(RestTemplateBuilderStubs.stub(), null));
    }

    @Test
    public void shouldCreatedWithNonNullArguments() {
        new HateoasTraversor.Factory(RestTemplateBuilderStubs.stub(), APPLICATION_JSON);
    }

    @Test
    public void shouldNotCreateTraversorsForNullRootUris() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> traversors.startAt(null));
    }

    @Test
    public void traverseRootOnly() {
        server
            .expect(requestTo(ROOT_URI))
            .andExpect(method(GET))
            .andRespond(withSuccess("someResponse", APPLICATION_JSON));

        final val response = traversors.startAt(ROOT_URI).perform();

        assertThat(response.getBody()).isEqualTo("someResponse");
    }

    @Test
    public void traverseGetHop() {
        server
            .expect(requestTo(ROOT_URI))
            .andExpect(method(GET))
            .andRespond(withSuccess(link("company", "http://root/company"), APP_V1));
        server
            .expect(requestTo("http://root/company"))
            .andExpect(method(GET))
            .andRespond(withSuccess(link("company", "http://any"), APP_V1));

        final val response = traversors.startAt(ROOT_URI).hop(f -> f.get("company")).perform();

        assertThat(response.getBody()).contains("http://any");
    }

    @Test
    public void traversePutHop() {
        server
            .expect(requestTo(ROOT_URI))
            .andExpect(method(GET))
            .andRespond(withSuccess(link("company", "http://root/company"), APP_V1));
        server
            .expect(requestTo("http://root/company"))
            .andExpect(method(PUT))
            .andRespond(withSuccess(link("any", "http://any"), APP_V1));

        final val response = traversors.startAt(ROOT_URI).hop(f -> f.put("company", "someRequest")).perform();

        assertThat(response.getBody()).contains("http://any");
    }

    @Test
    public void traversePostHop() {
        server
            .expect(requestTo(ROOT_URI))
            .andExpect(method(GET))
            .andRespond(withSuccess(link("company", "http://root/company"), APP_V1));
        server
            .expect(requestTo("http://root/company"))
            .andExpect(method(POST))
            .andRespond(withSuccess(link("any", "http://any"), APP_V1));

        final val response = traversors
            .startAt(ROOT_URI)
            .hop(f -> f.post("company", "someRequest"))
            .perform();

        assertThat(response.getBody()).contains("http://any");
    }

    @Test
    public void traverseMultipleHops() {
        server
            .expect(requestTo(ROOT_URI))
            .andExpect(method(GET))
            .andRespond(withSuccess(link("company", "http://root/company"), APP_V1));
        server
            .expect(requestTo("http://root/company"))
            .andExpect(method(GET))
            .andRespond(withSuccess(link("president", "http://root/president"), APP_V1));
        server
            .expect(requestTo("http://root/president"))
            .andExpect(method(GET))
            .andRespond(withSuccess(link("any", "http://any"), APP_V1));

        final val response = traversors
            .startAt(ROOT_URI)
            .hop(f -> f.get("company"))
            .hop(f -> f.get("president"))
            .perform();

        assertThat(response.getBody()).contains("http://any");
    }

    @Test
    public void shouldHandleClientErrors() {
        server
            .expect(requestTo(ROOT_URI))
            .andExpect(method(GET))
            .andRespond(withBadRequest().contentType(APPLICATION_JSON).body("someResponse"));

        final val response = traversors.startAt(ROOT_URI).perform();

        assertThat(response.getStatusCode()).isEqualTo(BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("someResponse");
    }

    @Test
    public void shouldHandleServerErrors() {
        server
            .expect(requestTo(ROOT_URI))
            .andExpect(method(GET))
            .andRespond(withServerError().contentType(APPLICATION_JSON).body("someResponse"));

        final val response = traversors.startAt(ROOT_URI).perform();

        assertThat(response.getStatusCode()).isEqualTo(INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("someResponse");
    }

    @TestConfiguration
    static class AppMediaTypeConfiguration {

        @Bean
        public MediaType appMediaType() {
            return APP_V1;
        }
    }
}

final class RestTemplateBuilderStubs {

    private RestTemplateBuilderStubs() {}

    static RestTemplateBuilder stub() {
        final val builder = mock(RestTemplateBuilder.class);
        when(builder.errorHandler(any())).thenReturn(builder);
        when(builder.build()).thenReturn(mock(RestTemplate.class));
        return builder;
    }
}

final class RestTemplateBuilderStubsTest {

    @Test
    public void shouldBuildRestTemplate() {
        assertThat(RestTemplateBuilderStubs.stub().build()).isInstanceOf(RestTemplate.class);
    }
}
