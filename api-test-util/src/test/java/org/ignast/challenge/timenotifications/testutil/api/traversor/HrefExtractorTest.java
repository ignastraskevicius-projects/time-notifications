package org.ignast.challenge.timenotifications.testutil.api.traversor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.ignast.challenge.timenotifications.testutil.api.traversor.HateoasLink.link;
import static org.mockito.Mockito.mock;
import static org.springframework.http.ResponseEntity.status;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

public final class HrefExtractorTest {

    private static final MediaType APP_V1 = MediaType.parseMediaType(
        "application/app.specific.media.type-v1.hal+json"
    );

    private static final int NOT_FOUND = 400;

    private static final int INTERNAL_SERVER_ERROR = 500;

    private static final int OK = 200;

    private static final int CREATED = 201;

    private final HrefExtractor extractor = new HrefExtractor(APP_V1);

    @Test
    public void shouldNotCreateWithNullMediaType() {
        assertThatExceptionOfType(NullPointerException.class).isThrownBy(() -> new HrefExtractor(null));
        new HrefExtractor(mock(MediaType.class));
    }

    @Test
    public void shouldExtractHref() {
        final val response = ResponseEntity
            .status(HttpStatus.OK)
            .contentType(APP_V1)
            .body(link("company", "companyUri"));

        assertThat(extractor.extractHref(response, "company")).isEqualTo("companyUri");
    }

    @Test
    public void shouldFailToExtractFromInvalidJsonResponses() {
        final val response = ResponseEntity.status(HttpStatus.OK).contentType(APP_V1).body("not-a-json");

        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> extractor.extractHref(response, "company"))
            .withMessage("Hop to 'company' failed: previous response is not a valid json");
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "{}",
            "{\"_lilnks\":{}}",
            "{\"_lilnks\":{\"company\":{}}}",
            "{\"_lilnks\":{\"client\":{\"href\":\"companyUri\"}}}",
        }
    )
    public void shouldFailToExtractNonexistentRel(final String notContainingCompanyRel) {
        final val response = ResponseEntity
            .status(HttpStatus.OK)
            .contentType(APP_V1)
            .body(notContainingCompanyRel);

        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> extractor.extractHref(response, "company"))
            .withMessage("Hop to 'company' failed: previous response does not contain rel to 'company'");
    }

    @Test
    public void shouldFailToExtractHrefFromResponseWithoutContentType() {
        final val response = ResponseEntity.status(HttpStatus.OK).body(HateoasLink.anyLink());

        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> extractor.extractHref(response, "any"))
            .withMessage("Hop to 'any' failed: previous response has no content-type specified");
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "text/xml",
            "application/json",
            "application/hal+jsosn",
            "application/vnd.stockinvesting.quotes.hal+json",
        }
    )
    public void shouldNotExtractHrefFromResponsesWithoutVersionedAppContentTypeSet(final String type) {
        final val mediaType = MediaType.parseMediaType(type);
        final val response = ResponseEntity
            .status(HttpStatus.OK)
            .contentType(mediaType)
            .body(HateoasLink.anyLink());

        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> extractor.extractHref(response, "any"))
            .withMessage("Hop to 'any' failed: previous response has unsupported content-type specified");
    }

    @ParameterizedTest
    @ValueSource(ints = { NOT_FOUND, INTERNAL_SERVER_ERROR })
    public void shouldNotExtractHrefFromResponsesWithNon2xxStatusCodes(final int status) {
        final val response = status(HttpStatus.valueOf(status)).body(HateoasLink.anyLink());

        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> extractor.extractHref(response, "any"))
            .withMessage("Hop to 'any' failed: previous interaction has failed");
    }

    @ParameterizedTest
    @ValueSource(ints = { OK, CREATED })
    public void shouldExtractHrefForResponsesWith2xxStatusCodes(final int status) {
        final val response = status(HttpStatus.valueOf(status))
            .contentType(APP_V1)
            .body(link("company", "companyUri"));

        assertThat(extractor.extractHref(response, "company")).isEqualTo("companyUri");
    }
}
