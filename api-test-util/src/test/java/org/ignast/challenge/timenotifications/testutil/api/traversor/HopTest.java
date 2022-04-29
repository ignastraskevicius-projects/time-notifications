package org.ignast.challenge.timenotifications.testutil.api.traversor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.ignast.challenge.timenotifications.testutil.api.traversor.HateoasLink.link;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.ResponseEntity.status;

import java.util.List;
import lombok.val;
import org.ignast.challenge.timenotifications.testutil.api.RestTemplateStubs;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public final class HopTest {

    private static final MediaType APP_V1 = MediaType.parseMediaType(
        "application/app.specific.media.type-v1.hal+json"
    );

    @Test
    public void factoryShouldNotBeCreatedWithNullAppMediaType() {
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> new Hop.Factory(null, mock(RestTemplate.class), mock(HrefExtractor.class)));
    }

    @Test
    public void factoryShouldNotBeCreatedWithNullRestTemplate() {
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> new Hop.Factory(APP_V1, null, mock(HrefExtractor.class)));
    }

    @Test
    public void factoryShouldNotBeCreatedWithNullHrefExtractor() {
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> new Hop.Factory(APP_V1, mock(RestTemplate.class), null));
    }

    @Test
    public void factoryShouldBeCreatedWithNonNullArguments() {
        new Hop.Factory(mock(MediaType.class), mock(RestTemplate.class), mock(HrefExtractor.class));
    }
}

@ExtendWith(MockitoExtension.class)
final class GetHopTest {

    private static final MediaType APP_V1 = MediaType.parseMediaType(
        "application/app.specific.media.type-v1.hal+json"
    );

    private static final String RESPONSE_FROM_SERVER = "someResponseFromServer";

    private final RestTemplate restTemplate = RestTemplateStubs.stubExchanging(RESPONSE_FROM_SERVER);

    private final HrefExtractor hrefExtractor = mock(HrefExtractor.class);

    private final Hop.Factory hopFactory = new Hop.Factory(APP_V1, restTemplate, hrefExtractor);

    @Mock
    private ResponseEntity<String> response;

    @Captor
    private ArgumentCaptor<HttpEntity<String>> entityCaptor;

    @Test
    public void hopsTraversalShouldNotAcceptNullEntities() {
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> hopFactory.get("any").traverse(null));
    }

    @Test
    public void getHopShouldNotAcceptNullRels() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> hopFactory.get(null));
    }

    @Test
    public void shouldTraverseGetHop() {
        final val linkToCompany = status(OK).contentType(APP_V1).body(link("company", "companyUri"));
        when(hrefExtractor.extractHref(linkToCompany, "company")).thenReturn("companyUri");

        final val companyResponse = hopFactory.get("company").traverse(linkToCompany);

        verify(restTemplate).exchange(eq("companyUri"), eq(GET), entityCaptor.capture(), eq(String.class));
        assertThat(entityCaptor.getValue().getHeaders().get("Accept")).isEqualTo(List.of(APP_V1.toString()));
        assertThat(companyResponse.getBody()).isEqualTo(RESPONSE_FROM_SERVER);
    }

    @Test
    public void getHopTraversalShouldFailWhenHrefCannotBeExtracted() {
        when(hrefExtractor.extractHref(response, "any")).thenThrow(TestException.class);

        assertThatExceptionOfType(TestException.class)
            .isThrownBy(() -> hopFactory.get("any").traverse(response));
    }

    private static class TestException extends RuntimeException {}
}

@ExtendWith(MockitoExtension.class)
final class GetCuriesHopTest {

    private static final MediaType APP_V1 = MediaType.parseMediaType(
        "application/app.specific.media.type-v1.hal+json"
    );

    private static final String RESPONSE_FROM_SERVER = "someResponseFromServer";

    private final RestTemplate restTemplate = RestTemplateStubs.stubExchanging(RESPONSE_FROM_SERVER);

    private final HrefExtractor hrefExtractor = mock(HrefExtractor.class);

    private final Hop.Factory hopFactory = new Hop.Factory(APP_V1, restTemplate, hrefExtractor);

    @Mock
    private ResponseEntity<String> response;

    @Captor
    private ArgumentCaptor<HttpEntity<String>> entityCaptor;

    @Test
    public void getHopShouldNotAcceptNullRels() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> hopFactory.get(null));
    }

    private static class TestException extends RuntimeException {}
}

@ExtendWith(MockitoExtension.class)
final class PutHopTest {

    private static final MediaType APP_V1 = MediaType.parseMediaType(
        "application/app.specific.media.type-v1.hal+json"
    );

    private static final String RESPONSE_FROM_SERVER = "someResponseFromServer";

    private final RestTemplate restTemplate = RestTemplateStubs.stubExchanging(RESPONSE_FROM_SERVER);

    private final HrefExtractor hrefExtractor = mock(HrefExtractor.class);

    private final Hop.Factory hopFactory = new Hop.Factory(APP_V1, restTemplate, hrefExtractor);

    @Mock
    private ResponseEntity<String> response;

    @Captor
    private ArgumentCaptor<HttpEntity<String>> entityCaptor;

    @Test
    public void hopsTraversalShouldNotAcceptNullEntities() {
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> hopFactory.put("any", "request").traverse(null));
    }

    @Test
    public void putHopShouldNotAcceptNullRels() {
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> hopFactory.put(null, "request"));
    }

    @Test
    public void putHopShouldNotAcceptNullRequests() {
        assertThatExceptionOfType(NullPointerException.class)
            .isThrownBy(() -> hopFactory.put("company", null));
    }

    @Test
    public void shouldTraversePutHop() {
        final val linkToClient = status(OK).contentType(APP_V1).body(link("client", "uri"));
        when(hrefExtractor.extractHref(linkToClient, "client")).thenReturn("clientUri");

        final val companyResponse = hopFactory.put("client", "hopRequest").traverse(linkToClient);

        verify(restTemplate).exchange(eq("clientUri"), eq(PUT), entityCaptor.capture(), eq(String.class));
        assertThat(entityCaptor.getValue().getHeaders().get("Content-Type"))
            .isEqualTo(List.of(APP_V1.toString()));
        assertThat(entityCaptor.getValue().getBody()).isEqualTo("hopRequest");
        assertThat(companyResponse.getBody()).isEqualTo(RESPONSE_FROM_SERVER);
    }

    @Test
    public void putHopTraversalShouldFailWhenHrefCannotBeExtracted() {
        when(hrefExtractor.extractHref(response, "any")).thenThrow(TestException.class);

        assertThatExceptionOfType(TestException.class)
            .isThrownBy(() -> hopFactory.put("any", "anyRequest").traverse(response));
    }

    private static class TestException extends RuntimeException {}
}
