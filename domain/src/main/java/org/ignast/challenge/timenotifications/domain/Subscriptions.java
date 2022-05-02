package org.ignast.challenge.timenotifications.domain;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.val;

public interface Subscriptions {
    public boolean subscribe(final PeriodicNotification subscription);

    public boolean unsubscribe(final URI uri);

    public boolean reschedule(final PeriodicNotification subscription);
}
