package org.ignast.challenge.timenotifications.testutil.api.traversor;

import static java.lang.String.format;

public final class HateoasLink {

    private HateoasLink() {}

    public static String link(final String rel, final String href) {
        return format("{\"_links\":{\"%s\":{\"href\":\"%s\"}}}", rel, href);
    }

    public static String anyLink() {
        return "{\"_links\":{\"any\":{\"href\":\"any\"}}}";
    }
}
