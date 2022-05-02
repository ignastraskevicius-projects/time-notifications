package org.ignast.challenge.timenotifications.domain;

import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import lombok.val;
import org.springframework.stereotype.Service;

@Service
public class ScheduleBackedNotifier {

    private final Map<URI, SubsequentOccurrences> schedule = new HashMap<>();
    private final UrisIndexedByTimestamp nextOccurrenceOfEachNotification = new UrisIndexedByTimestamp();
    private final TimestampTransformingNotificationSender notificationSender;

    ScheduleBackedNotifier(final TimestampTransformingNotificationSender notificationSender) {
        this.notificationSender = notificationSender;
    }

    void subscribe(final PeriodicNotification notification, final Timestamp currentTimestamp) {
        schedule.put(
            notification.subscriptionUri(),
            new SubsequentOccurrences(currentTimestamp, notification.frequencyInSeconds())
        );
        nextOccurrenceOfEachNotification
            .getScheduledNotificationsAt(currentTimestamp)
            .add(notification.subscriptionUri());
    }

    void unsubscribe(final URI uri) {
        val scheduledAt = schedule.get(uri).nextOccurrence();
        nextOccurrenceOfEachNotification.getScheduledNotificationsAt(scheduledAt).remove(uri);
        schedule.remove(uri);
    }

    void notify(final Timestamp currentTimestamp) {
        sendNotifications(currentTimestamp);
        rollNotificationsToNextOccurrences(currentTimestamp);
    }

    private void sendNotifications(final Timestamp currentTimestamp) {
        val notificationsToSendNow = nextOccurrenceOfEachNotification.getScheduledNotificationsAt(
            currentTimestamp
        );
        notificationsToSendNow.stream().forEach(c -> sendNotification(c, currentTimestamp));
    }

    private void sendNotification(final URI location, final Timestamp timestamp) {
        notificationSender.send(location, timestamp);
    }

    private void rollNotificationsToNextOccurrences(final Timestamp baseTimestamp) {
        val notificationsToRoll = nextOccurrenceOfEachNotification.getScheduledNotificationsAt(baseTimestamp);
        Iterator<URI> iter = notificationsToRoll.iterator();
        while (iter.hasNext()) {
            val uri = iter.next();
            iter.remove();
            val nextOccurrence = baseTimestamp.plusSeconds(schedule.get(uri).frequencyInSeconds());
            scheduleNextOccurrence(uri, nextOccurrence);
        }
    }

    private void scheduleNextOccurrence(final URI uri, final Timestamp nextOccurrence) {
        nextOccurrenceOfEachNotification.getScheduledNotificationsAt(nextOccurrence).add(uri);
        schedule.computeIfPresent(uri, (k, v) -> v.rollToNextOccurrence());
    }

    private record SubsequentOccurrences(Timestamp nextOccurrence, int frequencyInSeconds) {
        SubsequentOccurrences rollToNextOccurrence() {
            return new SubsequentOccurrences(
                nextOccurrence().plusSeconds(frequencyInSeconds),
                frequencyInSeconds
            );
        }
    }

    @Service
    record TimestampTransformingNotificationSender(NotificationSender notificationSender) {
        void send(final URI uri, final Timestamp timestamp) {
            notificationSender.send(uri, toUtcTime(timestamp));
        }

        private ZonedDateTime toUtcTime(final Timestamp timestamp) {
            long millis = timestamp.getValueInSeconds() * 1000l;
            ZonedDateTime utcTime = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC"));
            return utcTime;
        }
    }
}
