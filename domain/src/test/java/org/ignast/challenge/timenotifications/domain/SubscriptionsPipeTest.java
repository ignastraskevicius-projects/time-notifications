package org.ignast.challenge.timenotifications.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.junit.jupiter.api.Test;

class SubscriptionsPipeTest {

    private static final URI URI = java.net.URI.create("http://someuri");
    private final ConcurrentLinkedQueue<AlterSubscriptions> pipe = new ConcurrentLinkedQueue<>();

    private final Subscriptions subscriptionPipe = new SubscriptionsPipe(pipe);

    @Test
    public void shouldRegisterSubscription() {
        boolean subscribed = subscriptionPipe.subscribe(new PeriodicNotification(URI, 3));

        assertThat(subscribed).isTrue();
        assertThat(pipe.poll()).isEqualTo(new AddSubscription(new PeriodicNotification(URI, 3)));
        assertThat(pipe.poll()).isNull();
    }

    @Test
    public void shouldNotRegisterSameSubscriptionTwice() {
        subscriptionPipe.subscribe(new PeriodicNotification(URI, 3));
        boolean subscribed = subscriptionPipe.subscribe(new PeriodicNotification(URI, 3));

        assertThat(subscribed).isFalse();
        assertThat(pipe.poll()).isEqualTo(new AddSubscription(new PeriodicNotification(URI, 3)));
        assertThat(pipe.poll()).isNull();
    }

    @Test
    public void shouldRegisterSameSubscriptionAfterUnregisteringIt() {
        subscriptionPipe.subscribe(new PeriodicNotification(URI, 3));
        subscriptionPipe.unsubscribe(URI);
        boolean subscribed = subscriptionPipe.subscribe(new PeriodicNotification(URI, 3));

        assertThat(subscribed).isTrue();
        assertThat(pipe.poll()).isEqualTo(new AddSubscription(new PeriodicNotification(URI, 3)));
        assertThat(pipe.poll()).isEqualTo(new RemoveSubscription(URI));
        assertThat(pipe.poll()).isEqualTo(new AddSubscription(new PeriodicNotification(URI, 3)));
        assertThat(pipe.poll()).isNull();
    }

    @Test
    public void shouldNotRescheduleUnregisteredUri() {
        boolean rescheduled = subscriptionPipe.reschedule(new PeriodicNotification(URI, 3));

        assertThat(rescheduled).isFalse();
        assertThat(pipe.poll()).isNull();
    }

    @Test
    public void shouldRescheduleSubscription() {
        subscriptionPipe.subscribe(new PeriodicNotification(URI, 3));
        boolean rescheduled = subscriptionPipe.reschedule(new PeriodicNotification(URI, 4));

        assertThat(rescheduled).isTrue();
        assertThat(pipe.poll()).isEqualTo(new AddSubscription(new PeriodicNotification(URI, 3)));
        assertThat(pipe.poll()).isEqualTo(new RemoveSubscription(URI));
        assertThat(pipe.poll()).isEqualTo(new AddSubscription(new PeriodicNotification(URI, 4)));
        assertThat(pipe.poll()).isNull();
    }

    @Test
    public void shouldRescheduleSubscriptionTwice() {
        subscriptionPipe.subscribe(new PeriodicNotification(URI, 3));
        subscriptionPipe.reschedule(new PeriodicNotification(URI, 4));
        boolean rescheduled = subscriptionPipe.reschedule(new PeriodicNotification(URI, 5));

        assertThat(rescheduled).isTrue();
        assertThat(pipe.poll()).isEqualTo(new AddSubscription(new PeriodicNotification(URI, 3)));
        assertThat(pipe.poll()).isEqualTo(new RemoveSubscription(URI));
        assertThat(pipe.poll()).isEqualTo(new AddSubscription(new PeriodicNotification(URI, 4)));
        assertThat(pipe.poll()).isEqualTo(new RemoveSubscription(URI));
        assertThat(pipe.poll()).isEqualTo(new AddSubscription(new PeriodicNotification(URI, 5)));
        assertThat(pipe.poll()).isNull();
    }

    @Test
    public void shouldNotUnregisterNotRegisteredSubscription() {
        boolean unsubscribed = subscriptionPipe.unsubscribe(URI);

        assertThat(unsubscribed).isFalse();
        assertThat(pipe.poll()).isNull();
    }

    @Test
    public void shouldUnregisterSubscription() {
        subscriptionPipe.subscribe(new PeriodicNotification(URI, 3));
        boolean unsubscribed = subscriptionPipe.unsubscribe(URI);

        assertThat(unsubscribed).isTrue();
        assertThat(pipe.poll()).isEqualTo(new AddSubscription(new PeriodicNotification(URI, 3)));
        assertThat(pipe.poll()).isEqualTo(new RemoveSubscription(URI));
        assertThat(pipe.poll()).isNull();
    }

    @Test
    public void shouldNotUnregisterSameSubscriptionTwice() {
        subscriptionPipe.subscribe(new PeriodicNotification(URI, 3));
        subscriptionPipe.unsubscribe(URI);
        boolean unsubscribed = subscriptionPipe.unsubscribe(URI);

        assertThat(unsubscribed).isFalse();
        assertThat(pipe.poll()).isEqualTo(new AddSubscription(new PeriodicNotification(URI, 3)));
        assertThat(pipe.poll()).isEqualTo(new RemoveSubscription(URI));
        assertThat(pipe.poll()).isNull();
    }
}
