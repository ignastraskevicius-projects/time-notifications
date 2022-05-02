package org.ignast.challenge.timenotifications.testutil.api.traversor;

import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;

import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public interface Hop {
    public abstract static class TraversableHop implements Hop {

        abstract ResponseEntity<String> traverse(ResponseEntity<String> response);
    }

    @RequiredArgsConstructor
    public static final class Factory {

        @NonNull
        private final MediaType appMediaType;

        @NonNull
        private final RestTemplate restTemplate;

        @NonNull
        private final HrefExtractor hrefExtractor;

        public TraversableHop put(final String rel, final String body) {
            return new HopWithBody(appMediaType, restTemplate, hrefExtractor, rel, body, HttpMethod.PUT);
        }

        public TraversableHop post(final String rel, final String body) {
            return new HopWithBody(appMediaType, restTemplate, hrefExtractor, rel, body, HttpMethod.POST);
        }

        public TraversableHop get(@NonNull final String rel) {
            return new BodylessHop(appMediaType, restTemplate, r -> hrefExtractor.extractHref(r, rel), GET);
        }

        public TraversableHop delete(@NonNull final String rel) {
            return new BodylessHop(
                appMediaType,
                restTemplate,
                r -> hrefExtractor.extractHref(r, rel),
                DELETE
            );
        }

        @AllArgsConstructor
        private static final class HopWithBody extends TraversableHop {

            private final MediaType appMediaType;

            private final RestTemplate restTemplate;

            private final HrefExtractor hrefExtractor;

            @NonNull
            private final String rel;

            @NonNull
            private final String body;

            private final HttpMethod method;

            @Override
            public ResponseEntity<String> traverse(@NonNull final ResponseEntity<String> response) {
                final val href = hrefExtractor.extractHref(response, rel);
                return restTemplate.exchange(href, method, contentTypeV1(body), String.class);
            }

            private HttpEntity<String> contentTypeV1(final String content) {
                final val headers = new HttpHeaders();
                headers.add("Content-Type", appMediaType.toString());
                return new HttpEntity<>(content, headers);
            }
        }

        @AllArgsConstructor
        private static final class BodylessHop extends TraversableHop {

            private final MediaType appMediaType;

            private final RestTemplate restTemplate;

            private final Function<ResponseEntity<String>, String> extractorHref;

            private final HttpMethod method;

            @Override
            ResponseEntity<String> traverse(@NonNull final ResponseEntity<String> previousResponse) {
                final val href = extractorHref.apply(previousResponse);
                return restTemplate.exchange(href, method, acceptV1(), String.class);
            }

            private HttpEntity<String> acceptV1() {
                final val headers = new HttpHeaders();
                headers.add("Accept", appMediaType.toString());
                return new HttpEntity<>(headers);
            }
        }
    }
}
