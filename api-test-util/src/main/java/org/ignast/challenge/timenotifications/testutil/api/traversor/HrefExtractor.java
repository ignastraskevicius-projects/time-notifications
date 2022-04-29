package org.ignast.challenge.timenotifications.testutil.api.traversor;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Objects.isNull;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

import lombok.NonNull;
import lombok.val;
import org.hamcrest.MatcherAssert;
import org.ignast.challenge.timenotifications.testutil.api.HateoasJsonMatchers;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public class HrefExtractor {

    private static final String LINKS = "_links";

    private static final String HREF = "href";

    private final MediaType appMediaType;

    public HrefExtractor(@NonNull final MediaType appMediaType) {
        this.appMediaType = appMediaType;
    }

    @SuppressWarnings("checkstyle:designforextension")
    protected String extractHref(final ResponseEntity<String> response, final String rel) {
        return new Extractor(response, rel).extractValidating(this::traverseToResourceHref);
    }

    private String traverseToResourceHref(final String json, final String rel) throws JSONException {
        MatcherAssert.assertThat(json, HateoasJsonMatchers.hasRel(rel).withHref());
        return new JSONObject(json).getJSONObject(LINKS).getJSONObject(rel).getString(HREF);
    }

    private static interface AssertingTraversor {
        public String traverseAsserting(final String json, final String rel)
            throws AssertionError, JSONException;
    }

    private final class Extractor {

        private final ResponseEntity<String> previousResponse;

        private final String rel;

        private Extractor(final ResponseEntity<String> previousResponse, final String rel) {
            this.previousResponse = previousResponse;
            this.rel = rel;
        }

        private String extractValidating(final AssertingTraversor traverseAsserting) {
            expectSuccessfulResponse();
            expectAppResponse();
            expectValidJson(previousResponse.getBody());
            return extractHref(traverseAsserting);
        }

        private String extractHref(final AssertingTraversor traverseAsserting) {
            try {
                return traverseAsserting.traverseAsserting(previousResponse.getBody(), rel);
            } catch (AssertionError | JSONException e) {
                final val message = format("previous response does not contain rel to '%s'", rel);
                throw new IllegalArgumentException(formatError(message));
            }
        }

        private JSONObject expectValidJson(final String previousResponseBody) {
            try {
                return new JSONObject(previousResponseBody);
            } catch (JSONException e) {
                throw new IllegalArgumentException(formatError("previous response is not a valid json"));
            }
        }

        private void expectSuccessfulResponse() {
            if (!asList(OK, CREATED).contains(previousResponse.getStatusCode())) {
                throw new IllegalArgumentException(formatError("previous interaction has failed"));
            }
        }

        private void expectAppResponse() {
            if (isNull(previousResponse.getHeaders().getContentType())) {
                final val message = "previous response has no content-type specified";
                throw new IllegalArgumentException(formatError(message));
            }
            if (!appMediaType.equals(previousResponse.getHeaders().getContentType())) {
                final val message = "previous response has unsupported content-type specified";
                throw new IllegalArgumentException(formatError(message));
            }
        }

        private String formatError(final String message) {
            return format("Hop to '%s' failed: ", rel) + message;
        }
    }
}
