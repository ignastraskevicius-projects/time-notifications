package org.ignast.challenge.timenotifications.api.root;

import static org.ignast.challenge.timenotifications.testutil.api.HateoasJsonMatchers.hasRel;
import static org.springframework.hateoas.MediaTypes.HAL_JSON;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(RootController.class)
class RootControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void rootResourceShouldSubscriptions() throws Exception {
        final val root = mockMvc.perform(get("/").accept(HAL_JSON));
        root
            .andExpect(status().isOk())
            .andExpect(header().string(CONTENT_TYPE, HAL_JSON_VALUE))
            .andExpect(content().string(hasRel("subscriptions:create").withHref()));
    }
}
