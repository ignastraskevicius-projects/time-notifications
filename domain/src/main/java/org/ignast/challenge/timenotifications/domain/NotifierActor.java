package org.ignast.challenge.timenotifications.domain;

import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;

@Service
class NotifierActor implements Runnable {

    private final PerTickJob perTickJob;

    NotifierActor(
        final ConcurrentLinkedQueue<AlterSubscriptions> inbox,
        final ScheduleBackedNotifier scheduleBackedNotifier
    ) {
        this.perTickJob = new PerTickJob(inbox, scheduleBackedNotifier);
    }

    @Override
    public void run() {
        for (var nextTick = Timestamp.current(); true; nextTick = nextTick.plusSeconds(1)) {
            perTickJob.readInboxAndNotifyOnTick(nextTick);
        }
    }

    static class PerTickJob {

        private final SchedulingNotificationsInbox inbox;

        private final ScheduleBackedNotifier scheduleBackedNotifier;

        PerTickJob(
            final ConcurrentLinkedQueue<AlterSubscriptions> inbox,
            final ScheduleBackedNotifier scheduleBackedNotifier
        ) {
            this.scheduleBackedNotifier = scheduleBackedNotifier;
            this.inbox = new SchedulingNotificationsInbox(inbox, scheduleBackedNotifier);
        }

        void readInboxAndNotifyOnTick(Timestamp nextTick) {
            while (!Timestamp.current().gte(nextTick)) {
                waitForNextTick();
            }
            val currentTimestamp = Timestamp.current();
            inbox.read(currentTimestamp);
            scheduleBackedNotifier.notify(currentTimestamp);
        }

        private void waitForNextTick() {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
    static class SchedulingNotificationsInbox {

        private final ConcurrentLinkedQueue<AlterSubscriptions> mail;
        private final ScheduleBackedNotifier scheduleBackedNotifier;

        void read(Timestamp currentTimestamp) {
            for (
                AlterSubscriptions instruction = mail.poll();
                instruction != null;
                instruction = mail.poll()
            ) {
                if (instruction instanceof AddSubscription) {
                    AddSubscription addInstruction = (AddSubscription) instruction;
                    PeriodicNotification notification = addInstruction.notification();
                    scheduleBackedNotifier.subscribe(notification, currentTimestamp);
                } else {
                    RemoveSubscription removeInstruction = (RemoveSubscription) instruction;
                    val uri = removeInstruction.uri();
                    scheduleBackedNotifier.unsubscribe(uri);
                }
            }
        }
    }
}
