package org.ignast.challenge.timenotifications.domain;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.val;
import org.springframework.stereotype.Service;

@Service
class SubscriptionsPipe implements Subscriptions {

    private final Set<URI> registeredUris = new HashSet<>();
    private ConcurrentLinkedQueue<AlterSubscriptions> pendingInstructions;

    SubscriptionsPipe(final ConcurrentLinkedQueue<AlterSubscriptions> pendingInstructions) {
        this.pendingInstructions = pendingInstructions;
    }

    @Override
    public boolean subscribe(final PeriodicNotification subscription) {
        val uri = subscription.subscriptionUri();
        val subscribedSuccessfully = !registeredUris.contains(uri);
        if (subscribedSuccessfully) {
            registeredUris.add(uri);
            pendingInstructions.offer(new AddSubscription(subscription));
        }
        return subscribedSuccessfully;
    }

    @Override
    public boolean unsubscribe(final URI uri) {
        val unsubscribedSuccessfully = registeredUris.contains(uri);
        if (unsubscribedSuccessfully) {
            registeredUris.remove(uri);
            pendingInstructions.offer(new RemoveSubscription(uri));
        }
        return unsubscribedSuccessfully;
    }

    @Override
    public boolean reschedule(final PeriodicNotification subscription) {
        val uri = subscription.subscriptionUri();
        val unsubscribed = unsubscribe(uri);
        if (unsubscribed) {
            return subscribe(subscription);
        } else {
            return false;
        }
    }
}
