package org.ignast.challenge.timenotifications.testutil.api;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.json.JSONException;
import org.json.JSONObject;

public final class HateoasJsonMatchers {

    private static final String LINKS = "_links";

    private static final String HREF = "href";

    private HateoasJsonMatchers() {}

    public static HasRel hasRel(final String relName) {
        return new HasRel(relName);
    }

    public static final class HasRel {

        private final String relName;

        public HasRel(final String relName) {
            this.relName = relName;
        }

        public TypeSafeMatcher<String> withHref() {
            return new ExistsRelWithHref(relName);
        }

        static class ExistsRelWithHref extends TypeSafeMatcher<String> {

            private final String relName;

            public ExistsRelWithHref(final String relName) {
                this.relName = relName;
            }

            @Override
            protected boolean matchesSafely(final String hateoasJson) {
                try {
                    new JSONObject(hateoasJson).getJSONObject(LINKS).getJSONObject(relName).getString(HREF);
                    return true;
                } catch (JSONException e) {
                    return false;
                }
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText(
                    String.format("HATEOAS json should contain a '%s' rel with a href", relName)
                );
            }
        }
    }
}
