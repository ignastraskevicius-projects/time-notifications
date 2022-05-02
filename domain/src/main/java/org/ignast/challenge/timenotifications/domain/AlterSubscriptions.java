package org.ignast.challenge.timenotifications.domain;

import java.net.URI;

interface AlterSubscriptions {}

record AddSubscription(PeriodicNotification notification) implements AlterSubscriptions {}

record RemoveSubscription(URI uri) implements AlterSubscriptions {}
