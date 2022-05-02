package org.ignast.challenge.timenotifications.domain;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.net.URI;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.stream.IntStream;
import lombok.val;
import org.ignast.challenge.timenotifications.domain.ScheduleBackedNotifier.TimestampTransformingNotificationSender;
import org.junit.jupiter.api.Test;

class ScheduleBackedNotifierTest {

    private static final URI URI_A = java.net.URI.create("http://someUriA");

    private static final URI URI_B = java.net.URI.create("http://someUriB");

    private final Timestamp TIMESTAMP = Timestamp.current().plusSeconds(10);

    private final TimestampTransformingNotificationSender notificationSender = mock(
        TimestampTransformingNotificationSender.class
    );

    private final ScheduleBackedNotifier scheduleBackedNotifier = new ScheduleBackedNotifier(
        notificationSender
    );

    @Test
    public void shouldNotNotifyWhenThereAreZeroSubscribers() {
        scheduleBackedNotifier.notify(TIMESTAMP);

        verify(notificationSender, never()).send(any(), any());
    }

    @Test
    public void shouldNotifySubscriber() {
        scheduleBackedNotifier.subscribe(new PeriodicNotification(URI_A, 5), TIMESTAMP);

        scheduleBackedNotifier.notify(TIMESTAMP);

        verify(notificationSender, times(1)).send(URI_A, TIMESTAMP);
    }

    @Test
    public void shouldNotNotifyImmediatelyRemovedSubscriber() {
        scheduleBackedNotifier.subscribe(new PeriodicNotification(URI_A, 5), TIMESTAMP);
        scheduleBackedNotifier.unsubscribe(URI_A);

        scheduleBackedNotifier.notify(TIMESTAMP);

        verify(notificationSender, never()).send(URI_A, TIMESTAMP);
    }

    @Test
    public void shouldNotifyImmediatelyResubscribedSubscriber() {
        scheduleBackedNotifier.subscribe(new PeriodicNotification(URI_A, 5), TIMESTAMP);
        scheduleBackedNotifier.unsubscribe(URI_A);
        scheduleBackedNotifier.subscribe(new PeriodicNotification(URI_A, 5), TIMESTAMP);

        scheduleBackedNotifier.notify(TIMESTAMP);

        verify(notificationSender, times(1)).send(URI_A, TIMESTAMP);
    }

    @Test
    public void onceNotifiedNextOccurrenceShouldBeRescheduled() {
        scheduleBackedNotifier.subscribe(new PeriodicNotification(URI_A, 5), TIMESTAMP);

        scheduleBackedNotifier.notify(TIMESTAMP);
        scheduleBackedNotifier.notify(TIMESTAMP);

        verify(notificationSender, times(1)).send(URI_A, TIMESTAMP);
    }

    @Test
    public void rescheduledResubscribedSubscriberShouldBeNotified() {
        scheduleBackedNotifier.subscribe(new PeriodicNotification(URI_A, 5), TIMESTAMP);
        scheduleBackedNotifier.notify(TIMESTAMP);
        scheduleBackedNotifier.unsubscribe(URI_A);
        scheduleBackedNotifier.subscribe(new PeriodicNotification(URI_A, 5), TIMESTAMP);

        scheduleBackedNotifier.notify(TIMESTAMP);

        verify(notificationSender, times(2)).send(URI_A, TIMESTAMP);
    }

    @Test
    public void shouldNotifyMultipleSubscribers() {
        scheduleBackedNotifier.subscribe(new PeriodicNotification(URI_A, 5), TIMESTAMP);
        scheduleBackedNotifier.subscribe(new PeriodicNotification(URI_B, 6), TIMESTAMP);

        scheduleBackedNotifier.notify(TIMESTAMP);

        verify(notificationSender, times(1)).send(URI_A, TIMESTAMP);
        verify(notificationSender, times(1)).send(URI_B, TIMESTAMP);
    }

    @Test
    public void shouldAllowToUnsubscribeToInitialSubscribers() {
        scheduleBackedNotifier.subscribe(new PeriodicNotification(URI_A, 5), TIMESTAMP);
        scheduleBackedNotifier.subscribe(new PeriodicNotification(URI_B, 6), TIMESTAMP);
        scheduleBackedNotifier.unsubscribe(URI_A);

        scheduleBackedNotifier.notify(TIMESTAMP);

        verify(notificationSender, never()).send(URI_A, TIMESTAMP);
        verify(notificationSender, times(1)).send(URI_B, TIMESTAMP);
    }

    @Test
    public void shouldAllowToUnsubscribeToLaterSubscribers() {
        scheduleBackedNotifier.subscribe(new PeriodicNotification(URI_A, 5), TIMESTAMP);
        scheduleBackedNotifier.subscribe(new PeriodicNotification(URI_B, 6), TIMESTAMP);
        scheduleBackedNotifier.unsubscribe(URI_B);

        scheduleBackedNotifier.notify(TIMESTAMP);

        verify(notificationSender, times(1)).send(URI_A, TIMESTAMP);
        verify(notificationSender, never()).send(URI_B, TIMESTAMP);
    }

    @Test
    public void shouldAllowToResubscribeToInitialSubscribers() {
        scheduleBackedNotifier.subscribe(new PeriodicNotification(URI_A, 5), TIMESTAMP);
        scheduleBackedNotifier.subscribe(new PeriodicNotification(URI_B, 6), TIMESTAMP);
        scheduleBackedNotifier.unsubscribe(URI_A);
        scheduleBackedNotifier.subscribe(new PeriodicNotification(URI_A, 5), TIMESTAMP);

        scheduleBackedNotifier.notify(TIMESTAMP);

        verify(notificationSender, times(1)).send(URI_A, TIMESTAMP);
        verify(notificationSender, times(1)).send(URI_B, TIMESTAMP);
    }

    @Test
    public void shouldAllowToResubscribeToLaterSubscribers() {
        scheduleBackedNotifier.subscribe(new PeriodicNotification(URI_A, 5), TIMESTAMP);
        scheduleBackedNotifier.subscribe(new PeriodicNotification(URI_B, 6), TIMESTAMP);
        scheduleBackedNotifier.unsubscribe(URI_B);
        scheduleBackedNotifier.subscribe(new PeriodicNotification(URI_B, 6), TIMESTAMP);

        scheduleBackedNotifier.notify(TIMESTAMP);

        verify(notificationSender, times(1)).send(URI_A, TIMESTAMP);
        verify(notificationSender, times(1)).send(URI_B, TIMESTAMP);
    }

    @Test
    public void shouldNotifyMultipleSubscribersAtTheirCorrespondingPeriods() {
        val periodA = 5;
        val periodB = 6;
        scheduleBackedNotifier.subscribe(new PeriodicNotification(URI_A, periodA), TIMESTAMP);
        scheduleBackedNotifier.subscribe(new PeriodicNotification(URI_B, periodB), TIMESTAMP);

        scheduleBackedNotifier.notify(TIMESTAMP);
        scheduleBackedNotifier.notify(TIMESTAMP.plusSeconds(periodA));
        scheduleBackedNotifier.notify(TIMESTAMP.plusSeconds(periodB));

        verify(notificationSender, times(1)).send(URI_A, TIMESTAMP.plusSeconds(periodA));
        verify(notificationSender, times(1)).send(URI_B, TIMESTAMP.plusSeconds(periodB));
    }

    @Test
    public void shouldNotifyMultipleSubscribersAtCoincidingPeriods() {
        val period = 5;
        scheduleBackedNotifier.subscribe(new PeriodicNotification(URI_A, period), TIMESTAMP);
        scheduleBackedNotifier.subscribe(new PeriodicNotification(URI_B, period), TIMESTAMP);

        scheduleBackedNotifier.notify(TIMESTAMP);
        scheduleBackedNotifier.notify(TIMESTAMP.plusSeconds(period));

        verify(notificationSender, times(1)).send(URI_A, TIMESTAMP.plusSeconds(period));
        verify(notificationSender, times(1)).send(URI_B, TIMESTAMP.plusSeconds(period));
    }

    @Test
    public void shouldNotifyMultipleSubscribersAtCoincidingPeriodsJoiningAtDifferentTimes() {
        val period = 5;
        scheduleBackedNotifier.subscribe(new PeriodicNotification(URI_A, period), TIMESTAMP);
        scheduleBackedNotifier.notify(TIMESTAMP);
        scheduleBackedNotifier.subscribe(
            new PeriodicNotification(URI_B, period),
            TIMESTAMP.plusSeconds(period)
        );

        scheduleBackedNotifier.notify(TIMESTAMP.plusSeconds(period));

        verify(notificationSender, times(1)).send(URI_A, TIMESTAMP.plusSeconds(period));
        verify(notificationSender, times(1)).send(URI_B, TIMESTAMP.plusSeconds(period));
    }

    @Test
    public void shouldNotifySubscriberPeriodically() {
        val period = 5;
        scheduleBackedNotifier.subscribe(new PeriodicNotification(URI_A, period), TIMESTAMP);

        scheduleBackedNotifier.notify(TIMESTAMP);
        scheduleBackedNotifier.notify(TIMESTAMP.plusSeconds(period));
        scheduleBackedNotifier.notify(TIMESTAMP.plusSeconds(period * 2));

        verify(notificationSender, times(1)).send(URI_A, TIMESTAMP);
        verify(notificationSender, times(1)).send(URI_A, TIMESTAMP.plusSeconds(period));
        verify(notificationSender, times(1)).send(URI_A, TIMESTAMP.plusSeconds(period * 2));
    }

    @Test
    public void shouldAllowSubscriberToChangeTheirPeriodsRightBeforeTheNotification() {
        val initialPeriod = 5;
        val changedPeriod = 6;
        scheduleBackedNotifier.subscribe(new PeriodicNotification(URI_A, initialPeriod), TIMESTAMP);
        scheduleBackedNotifier.notify(TIMESTAMP);
        scheduleBackedNotifier.unsubscribe(URI_A);
        scheduleBackedNotifier.subscribe(
            new PeriodicNotification(URI_A, changedPeriod),
            TIMESTAMP.plusSeconds(initialPeriod)
        );

        scheduleBackedNotifier.notify(TIMESTAMP.plusSeconds(initialPeriod));
        scheduleBackedNotifier.notify(TIMESTAMP.plusSeconds(initialPeriod).plusSeconds(changedPeriod));

        verify(notificationSender, times(1)).send(URI_A, TIMESTAMP);
        verify(notificationSender, times(1)).send(URI_A, TIMESTAMP.plusSeconds(initialPeriod));
        verify(notificationSender, times(1))
            .send(URI_A, TIMESTAMP.plusSeconds(initialPeriod).plusSeconds(changedPeriod));
    }

    @Test
    public void shouldAllowSubscriberToChangeTheirPeriodsBeforeTheirScheduledTime() {
        val randomMoment = 2;
        val initialPeriod = 5;
        val changedPeriod = 6;
        scheduleBackedNotifier.subscribe(new PeriodicNotification(URI_A, initialPeriod), TIMESTAMP);
        scheduleBackedNotifier.notify(TIMESTAMP);
        scheduleBackedNotifier.unsubscribe(URI_A);
        scheduleBackedNotifier.subscribe(
            new PeriodicNotification(URI_A, changedPeriod),
            TIMESTAMP.plusSeconds(randomMoment)
        );

        scheduleBackedNotifier.notify(TIMESTAMP.plusSeconds(randomMoment));
        scheduleBackedNotifier.notify(TIMESTAMP.plusSeconds(initialPeriod));
        scheduleBackedNotifier.notify(TIMESTAMP.plusSeconds(randomMoment).plusSeconds(changedPeriod));
        scheduleBackedNotifier.notify(TIMESTAMP.plusSeconds(initialPeriod).plusSeconds(changedPeriod));

        verify(notificationSender, times(1)).send(URI_A, TIMESTAMP);
        verify(notificationSender, times(1)).send(URI_A, TIMESTAMP.plusSeconds(randomMoment));
        verify(notificationSender, never()).send(URI_A, TIMESTAMP.plusSeconds(initialPeriod));
        verify(notificationSender, times(1))
            .send(URI_A, TIMESTAMP.plusSeconds(randomMoment).plusSeconds(changedPeriod));
        verify(notificationSender, never())
            .send(URI_A, TIMESTAMP.plusSeconds(initialPeriod).plusSeconds(changedPeriod));
    }

    @Test
    public void shouldJitScheduledOnePeriodAtATimeForPerformanceReasons() {
        val period = 5;
        scheduleBackedNotifier.subscribe(new PeriodicNotification(URI_A, period), TIMESTAMP);

        scheduleBackedNotifier.notify(TIMESTAMP);
        scheduleBackedNotifier.notify(TIMESTAMP.plusSeconds(period * 2));

        verify(notificationSender, times(1)).send(URI_A, TIMESTAMP);
        verify(notificationSender, never()).send(URI_A, TIMESTAMP.plusSeconds(period * 2));
    }

    @Test
    public void shouldNotNotifyIfPeriodHasNotYetPast() {
        val period = 3;
        scheduleBackedNotifier.subscribe(new PeriodicNotification(URI_A, period), TIMESTAMP);

        scheduleBackedNotifier.notify(TIMESTAMP);
        scheduleBackedNotifier.notify(TIMESTAMP.plusSeconds(1));
        scheduleBackedNotifier.notify(TIMESTAMP.plusSeconds(2));
        scheduleBackedNotifier.notify(TIMESTAMP.plusSeconds(4));

        verify(notificationSender, times(1)).send(any(), any());
    }

    @Test
    public void shouldNotifySubscriberWithLongPeriods() {
        val period = 5;
        scheduleBackedNotifier.subscribe(new PeriodicNotification(URI_A, period), TIMESTAMP);

        IntStream
            .range(0, 15)
            .forEach(i -> {
                scheduleBackedNotifier.notify(TIMESTAMP.plusSeconds(period * i));

                verify(notificationSender).send(URI_A, TIMESTAMP.plusSeconds(period * i));
            });
    }
}

class TimestampTransformingNotificationSenderTest {

    private static final URI ANY_URI = URI.create("http://any");
    private static final int MILLIS_EPOCH_UTC_YEAR_2030 = 1893456001;
    private final NotificationSender notificationSender = mock(NotificationSender.class);

    private final ZonedDateTime UTC_YEAR_2030 = ZonedDateTime.of(2030, 1, 1, 0, 0, 1, 0, ZoneId.of("UTC"));

    private final TimestampTransformingNotificationSender timestampTransformingNotificationSender = new TimestampTransformingNotificationSender(
        notificationSender
    );

    @Test
    public void shouldTransformTimestampToDate() {
        val now = Timestamp.current();
        val valueInSeconds = now.getValueInSeconds();
        val year2030 = now.plusSeconds(MILLIS_EPOCH_UTC_YEAR_2030 - valueInSeconds);

        timestampTransformingNotificationSender.send(ANY_URI, year2030);

        verify(notificationSender).send(ANY_URI, UTC_YEAR_2030);
    }
}
