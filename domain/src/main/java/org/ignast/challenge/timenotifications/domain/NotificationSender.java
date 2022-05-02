package org.ignast.challenge.timenotifications.domain;

import java.net.URI;
import java.time.ZonedDateTime;

public interface NotificationSender {
    public void send(final URI location, final ZonedDateTime time);
}
