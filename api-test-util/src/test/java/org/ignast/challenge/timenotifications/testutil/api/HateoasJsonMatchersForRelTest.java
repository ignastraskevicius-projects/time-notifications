package org.ignast.challenge.timenotifications.testutil.api;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.val;
import org.hamcrest.Matcher;
import org.hamcrest.StringDescription;
import org.junit.jupiter.api.Test;

final class HateoasJsonMatchersForRelTest {

    private final Matcher<String> matcher = HateoasJsonMatchers.hasRel("link:link").withHref();

    @Test
    public void messageShouldIndicateExpectationAndActualOutcome() {
        final val testJson = "{}";
        final val desc = new StringDescription();

        matcher.describeTo(desc);
        matcher.describeMismatch(testJson, desc);

        assertThat(desc.toString())
            .contains("HATEOAS json should contain a 'link:link' rel with a href")
            .contains(testJson);
    }

    @Test
    public void shouldNotMatchJsonWithoutHateoasLinks() {
        assertThat(matcher.matches("{}")).isFalse();
    }

    @Test
    public void shouldNotMatchJsonWithoutSpecifiedLinks() {
        assertThat(matcher.matches("{\"_links\":{}}")).isFalse();
    }

    @Test
    public void shouldNotMatchJsonHavingRelWithoutHref() {
        assertThat(matcher.matches("{\"_links\":{\"link:link\":{}}}")).isFalse();
    }

    @Test
    public void shouldMatchAnyHrefs() {
        assertThat(matcher.matches("{\"_links\":{\"link:link\":{\"href\":\"any\"}}}")).isTrue();
    }
}
