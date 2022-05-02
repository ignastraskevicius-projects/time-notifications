package org.ignast.challenge.timenotifications.domain;

import java.net.URI;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import lombok.val;

public class UrisIndexedByTimestamp {

    private static final int SECONDS_IN_OVER_4_HOURS = 4 * 60 * 60 + 1;
    private final Set<URI>[] timeline = (Set<URI>[]) new Set[SECONDS_IN_OVER_4_HOURS];
    private final Timestamp initTimestamp;

    UrisIndexedByTimestamp() {
        initTimestamp = Timestamp.current();

        //TODO:if memory was not an issue at all, HashSet could be used instead, giving a time complexity of O(1)
        Arrays.setAll(timeline, i -> new TreeSet<>());
    }

    Set<URI> getScheduledNotificationsAt(final Timestamp timestamp) {
        val i = (timestamp.getValueInSeconds() - initTimestamp.getValueInSeconds()) % SECONDS_IN_OVER_4_HOURS;
        return timeline[i];
    }
}
