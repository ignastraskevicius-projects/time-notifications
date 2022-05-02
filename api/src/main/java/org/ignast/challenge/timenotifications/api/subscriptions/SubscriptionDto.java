package org.ignast.challenge.timenotifications.api.subscriptions;

import java.net.URI;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.ignast.challenge.timenotifications.domain.PeriodicNotification;

public record SubscriptionDto(@NotNull URI subscriptionUri, @Valid @NotNull TimePeriodDto frequency) {
    public PeriodicNotification toPeriodicNotification() {
        return new PeriodicNotification(subscriptionUri, frequency.amount());
    }
}
