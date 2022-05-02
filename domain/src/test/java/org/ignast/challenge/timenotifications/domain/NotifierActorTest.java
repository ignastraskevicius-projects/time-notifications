package org.ignast.challenge.timenotifications.domain;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.net.URI;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;

class ActorInboxTest {

    private static final Timestamp TIMESTAMP = Timestamp.current();
    private static final URI URI = java.net.URI.create("http://any");

    private final ConcurrentLinkedQueue<AlterSubscriptions> underlyingInbox = new ConcurrentLinkedQueue<>();

    private final ScheduleBackedNotifier scheduleBackedNotifier = mock(ScheduleBackedNotifier.class);

    private final NotifierActor.SchedulingNotificationsInbox schedulingInbox = new NotifierActor.SchedulingNotificationsInbox(
        underlyingInbox,
        scheduleBackedNotifier
    );

    @Test
    public void shouldNotScheduleWhenInboxIsEmpty() {
        schedulingInbox.read(TIMESTAMP);

        verifyNoInteractions(scheduleBackedNotifier);
    }

    @Test
    public void shouldInboxAndNotify() {
        underlyingInbox.offer(new RemoveSubscription(URI));
        underlyingInbox.offer(new AddSubscription(new PeriodicNotification(URI, 4)));

        schedulingInbox.read(TIMESTAMP);

        val inOrder = inOrder(scheduleBackedNotifier);
        inOrder.verify(scheduleBackedNotifier).unsubscribe(URI);
        inOrder.verify(scheduleBackedNotifier).subscribe(new PeriodicNotification(URI, 4), TIMESTAMP);
    }
}

class ActorPerTickJobTest {

    private static final Timestamp TIMESTAMP = Timestamp.current();
    private static final URI URI = java.net.URI.create("http://any");

    private final ConcurrentLinkedQueue<AlterSubscriptions> underlyingInbox = new ConcurrentLinkedQueue<>();

    private final ScheduleBackedNotifier scheduleBackedNotifier = mock(ScheduleBackedNotifier.class);

    private final NotifierActor.PerTickJob perTickJob = new NotifierActor.PerTickJob(
        underlyingInbox,
        scheduleBackedNotifier
    );

    @Test
    public void shouldNotScheduleWhenInboxIsEmpty() {
        perTickJob.readInboxAndNotifyOnTick(TIMESTAMP);

        verify(scheduleBackedNotifier).notify(argThat(atOr1SecondAfter(TIMESTAMP)));
    }

    @Test
    public void shouldInboxAndNotify() {
        underlyingInbox.offer(new RemoveSubscription(URI));
        underlyingInbox.offer(new AddSubscription(new PeriodicNotification(URI, 4)));

        perTickJob.readInboxAndNotifyOnTick(TIMESTAMP);

        val inOrder = inOrder(scheduleBackedNotifier);
        verify(scheduleBackedNotifier).unsubscribe(URI);
        verify(scheduleBackedNotifier)
            .subscribe(eq(new PeriodicNotification(URI, 4)), argThat(atOr1SecondAfter(TIMESTAMP)));
        verify(scheduleBackedNotifier).notify(argThat(atOr1SecondAfter(TIMESTAMP)));
    }

    private ArgumentMatcher<Timestamp> atOr1SecondAfter(final Timestamp timestamp) {
        return m -> m.gte(TIMESTAMP) && !m.gte(TIMESTAMP.plusSeconds(2));
    }
}
