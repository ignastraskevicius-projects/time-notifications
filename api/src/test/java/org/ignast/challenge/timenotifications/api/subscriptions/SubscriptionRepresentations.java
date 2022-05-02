package org.ignast.challenge.timenotifications.api.subscriptions;

public class SubscriptionRepresentations {

    static String every5SecsAt(final String url) {
        return String.format(
            "{\"subscriptionUri\":\"%s\",\"frequency\":{\"amount\":5,\"timeUnit\":\"second\"}}",
            url
        );
    }

    static String every6SecsAt(final String url) {
        return String.format(
            "{\"subscriptionUri\":\"%s\",\"frequency\":{\"amount\":6,\"timeUnit\":\"second\"}}",
            url
        );
    }
}
