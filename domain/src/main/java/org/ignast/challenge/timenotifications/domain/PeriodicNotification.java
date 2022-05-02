package org.ignast.challenge.timenotifications.domain;

import java.net.URI;

public record PeriodicNotification(URI subscriptionUri, int frequencyInSeconds) {}
