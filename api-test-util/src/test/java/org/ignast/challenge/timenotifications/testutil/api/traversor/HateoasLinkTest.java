package org.ignast.challenge.timenotifications.testutil.api.traversor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.ignast.challenge.timenotifications.testutil.api.traversor.HateoasLink.anyLink;
import static org.ignast.challenge.timenotifications.testutil.api.traversor.HateoasLink.link;

import lombok.val;
import org.junit.jupiter.api.Test;

final class HateoasLinkTest {

    @Test
    public void shouldCreateLink() {
        final val link = link("company", "companyUri");

        assertThat(link).isEqualTo("{\"_links\":{\"company\":{\"href\":\"companyUri\"}}}");
    }

    @Test
    public void shouldCreateAnyLink() {
        assertThat(anyLink()).isEqualTo("{\"_links\":{\"any\":{\"href\":\"any\"}}}");
    }
}
