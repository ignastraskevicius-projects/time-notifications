package org.ignast.challenge.timenotifications.api.subscriptions;

import static org.ignast.challenge.timenotifications.api.subscriptions.SubscriptionRepresentations.every5SecsAt;
import static org.ignast.challenge.timenotifications.api.subscriptions.SubscriptionRepresentations.every6SecsAt;
import static org.ignast.challenge.timenotifications.testutil.api.HateoasJsonMatchers.hasRel;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.hateoas.MediaTypes.HAL_JSON;
import static org.springframework.hateoas.MediaTypes.HAL_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import lombok.val;
import org.ignast.challenge.timenotifications.domain.PeriodicNotification;
import org.ignast.challenge.timenotifications.domain.Subscriptions;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

@WebMvcTest
class SubscriptionCreationControllerTest {

    private static final String URL = "http://localhost:8081";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Subscriptions subscriptions;

    @Test
    public void shouldRejectSubscriptionCreationIfBodyIsEmpty() throws Exception {
        mockMvc.perform(post("/subscriptions").contentType(HAL_JSON)).andExpect(status().isBadRequest());
    }

    @Test
    public void shouldRejectSubscriptionCreationIfBodyIsNotJson() throws Exception {
        mockMvc
            .perform(post("/subscriptions").contentType(HAL_JSON).content("not-json"))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldCreateSubscription() throws Exception {
        when(subscriptions.subscribe(any())).thenReturn(true);

        val result = mockMvc
            .perform(post("/subscriptions").contentType(HAL_JSON_VALUE).content(every5SecsAt(URL)))
            .andReturn();

        mockMvc
            .perform(asyncDispatch(result))
            .andExpect(status().isCreated())
            .andExpect(header().string("Content-Type", HAL_JSON_VALUE))
            .andExpect(resourceContentMatchesJson(every5SecsAt(URL)))
            .andExpect(content().string(hasRel("self").withHref()));
    }

    @Test
    public void shouldNotCreationDuplicateSubscriptions() throws Exception {
        val result = mockMvc
            .perform(post("/subscriptions").contentType(HAL_JSON_VALUE).content(every5SecsAt(URL)))
            .andReturn();

        mockMvc
            .perform(asyncDispatch(result))
            .andExpect(status().isBadRequest())
            .andExpect(content().json("{\"error\":\"URI is already registered\"}"));
    }

    @Test
    public void shouldForwardSubscriptionToDomain() throws Exception {
        when(subscriptions.subscribe(new PeriodicNotification(URI.create(URL), 5))).thenReturn(true);

        val result = mockMvc
            .perform(post("/subscriptions").contentType(HAL_JSON_VALUE).content(every5SecsAt(URL)))
            .andReturn();

        mockMvc.perform(asyncDispatch(result)).andExpect(status().isCreated());
    }

    @Test
    public void shouldRejectCreationIfSubscriptionHasNoUri() throws Exception {
        mockMvc
            .perform(
                post("/subscriptions")
                    .contentType(HAL_JSON)
                    .content("{\"frequency\":{\"amount\":4,\"timeUnit\":\"second\"}}")
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldRejectCreationIfSubscriptionHasNoFrequency() throws Exception {
        mockMvc
            .perform(
                post("/subscriptions")
                    .contentType(HAL_JSON)
                    .content("{\"subscriptionUri\":\"http://localhost:8081\"}")
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldRejectCreationIfSubscriptionHasNoFrequencyAmount() throws Exception {
        mockMvc
            .perform(
                post("/subscriptions")
                    .contentType(HAL_JSON)
                    .content(
                        "{\"subscriptionUri\":\"http://localhost:8081\",\"frequency\":{\"timeUnit\":\"second\"}}"
                    )
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldRejectCreationIfSubscriptionHasNoFrequencyTimeUnit() throws Exception {
        mockMvc
            .perform(
                post("/subscriptions")
                    .contentType(HAL_JSON)
                    .content("{\"subscriptionUri\":\"http://localhost:8081\",\"frequency\":{\"amount\":5}}")
            )
            .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldRejectCreationIfSubscriptionHasFrequencyTimeUnitOtherThanSecond() throws Exception {
        mockMvc
            .perform(
                post("/subscriptions")
                    .contentType(HAL_JSON)
                    .content(
                        "{\"subscriptionUri\":\"http://localhost:8081\",\"frequency\":{\"amount\":5,\"timeUnit\":\"minute\"}}"
                    )
            )
            .andExpect(status().isBadRequest());
    }

    public static ResultMatcher resourceContentMatchesJson(final String expectedJson) {
        return result -> {
            final val expected = new JSONObject(expectedJson);
            final val actualJson = new JSONObject(
                result.getResponse().getContentAsString(StandardCharsets.UTF_8)
            );
            actualJson.remove("_links");
            JSONAssert.assertEquals(expected, actualJson, JSONCompareMode.NON_EXTENSIBLE);
        };
    }
}

@WebMvcTest
class SubscriptionRemovalControllerTest {

    private static final String URL = "http://localhost:8081";

    private static final String BASE64_URL = "aHR0cDovL2xvY2FsaG9zdDo4MDgx";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Subscriptions subscriptions;

    @Test
    public void shouldRemoveSubscription() throws Exception {
        when(subscriptions.unsubscribe(any())).thenReturn(true);

        val result = mockMvc.perform(delete("/subscriptions/" + BASE64_URL)).andReturn();

        mockMvc.perform(asyncDispatch(result)).andExpect(status().isOk());
    }

    @Test
    public void shouldNotRemoveNonexistentSubscriptions() throws Exception {
        val result = mockMvc.perform(delete("/subscriptions/" + BASE64_URL)).andReturn();

        mockMvc.perform(asyncDispatch(result)).andExpect(status().isNotFound());
    }

    @Test
    public void shouldNotAttemptToRemoveBrokenSubscriptions() throws Exception {
        val result = mockMvc.perform(delete("/subscriptions/identifiedChangedManually")).andReturn();

        mockMvc.perform(asyncDispatch(result)).andExpect(status().isNotFound());
    }

    @Test
    public void shouldForwardSubscriptionRemovalToDomain() throws Exception {
        when(subscriptions.unsubscribe(URI.create(URL))).thenReturn(true);

        val result = mockMvc.perform(delete("/subscriptions/" + BASE64_URL)).andReturn();

        mockMvc.perform(asyncDispatch(result)).andExpect(status().isOk());
    }
}

@WebMvcTest
class SubscriptionRescheduleControllerTest {

    private static final String URL = "http://localhost:8081";

    private static final String BASE64_URL = "aHR0cDovL2xvY2FsaG9zdDo4MDgx";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Subscriptions subscriptions;

    @Test
    public void shouldRejectReschedulingIfBodyIsNotJson() throws Exception {
        mockMvc
            .perform(put("/subscriptions/" + BASE64_URL).contentType(HAL_JSON).content("not-json"))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldRescheduleSubscription() throws Exception {
        when(subscriptions.reschedule(any())).thenReturn(true);

        val result = mockMvc
            .perform(
                put("/subscriptions/" + BASE64_URL)
                    .contentType(HAL_JSON_VALUE)
                    .content(every6SecsAt("anyUrl"))
            )
            .andReturn();

        mockMvc
            .perform(asyncDispatch(result))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Type", HAL_JSON_VALUE))
            .andExpect(resourceContentMatchesJson(every6SecsAt(URL)))
            .andExpect(content().string(hasRel("self").withHref()));
    }

    @Test
    public void shouldNotRescheduleNonexistentSubscriptions() throws Exception {
        val result = mockMvc
            .perform(
                put("/subscriptions/" + BASE64_URL)
                    .contentType(HAL_JSON_VALUE)
                    .content(every6SecsAt("anyUrl"))
            )
            .andReturn();

        mockMvc.perform(asyncDispatch(result)).andExpect(status().isNotFound());
    }

    @Test
    public void shouldForwardSubscriptionToDomain() throws Exception {
        when(subscriptions.reschedule(new PeriodicNotification(URI.create(URL), 6))).thenReturn(true);

        val result = mockMvc
            .perform(
                put("/subscriptions/" + BASE64_URL)
                    .contentType(HAL_JSON_VALUE)
                    .content(every6SecsAt("anyUrl"))
            )
            .andReturn();

        mockMvc.perform(asyncDispatch(result)).andExpect(status().isOk());
    }

    @Test
    public void shouldNotAttemptToRescheduleBrokenSubscriptions() throws Exception {
        val result = mockMvc
            .perform(
                put("/subscriptions/identifiedChangedManually")
                    .contentType(HAL_JSON_VALUE)
                    .content(every6SecsAt("anyUrl"))
            )
            .andReturn();

        mockMvc.perform(asyncDispatch(result)).andExpect(status().isNotFound());
    }

    @Test
    public void shouldRejectReschedulingIfSubscriptionHasNoUri() throws Exception {
        mockMvc
            .perform(
                put("/subscriptions/" + BASE64_URL)
                    .contentType(HAL_JSON)
                    .content("{\"frequency\":{\"amount\":4,\"timeUnit\":\"second\"}}")
            )
            .andExpect(status().isBadRequest());
    }

    public static ResultMatcher resourceContentMatchesJson(final String expectedJson) {
        return result -> {
            final val expected = new JSONObject(expectedJson);
            final val actualJson = new JSONObject(
                result.getResponse().getContentAsString(StandardCharsets.UTF_8)
            );
            actualJson.remove("_links");
            JSONAssert.assertEquals(expected, actualJson, JSONCompareMode.NON_EXTENSIBLE);
        };
    }
}
