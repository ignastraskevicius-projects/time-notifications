package org.ignast.challenge.timenotifications.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import lombok.val;
import org.junit.jupiter.api.Test;

class UrisIndexedByTimestampTest {

    private static final URI URI_A = URI.create("http://abc.com");

    private static final int SECONDS_IN_4_HOURS = 4 * 60 * 60;

    private final UrisIndexedByTimestamp index = new UrisIndexedByTimestamp();

    @Test
    public void indexShouldInitiallyNotContainUris() {
        val now = Timestamp.current();

        assertThat(index.getScheduledNotificationsAt(now)).isEmpty();
    }

    @Test
    public void indexShouldPreserveInsertedUris() {
        val now = Timestamp.current();

        index.getScheduledNotificationsAt(now).add(URI_A);

        assertThat(index.getScheduledNotificationsAt(now)).hasSize(1);
        assertThat(index.getScheduledNotificationsAt(now)).contains(URI_A);
    }

    @Test
    public void indexShouldInsertUrisOnlyToSpecifiedTimestamps() {
        val now = Timestamp.current();

        index.getScheduledNotificationsAt(now).add(URI_A);

        assertThat(index.getScheduledNotificationsAt(now.plusSeconds(1))).hasSize(0);
    }

    @Test
    public void indexShouldNotBeBeCyclicalFor4Hours() {
        val now = Timestamp.current();

        index.getScheduledNotificationsAt(now).add(URI_A);

        assertThat(index.getScheduledNotificationsAt(now.plusSeconds(SECONDS_IN_4_HOURS))).hasSize(0);
    }

    @Test
    public void indexCouldBeCyclicalAfter4Hours() {
        val now = Timestamp.current();

        index.getScheduledNotificationsAt(now).add(URI_A);

        assertThat(index.getScheduledNotificationsAt(now.plusSeconds(SECONDS_IN_4_HOURS + 1))).hasSize(1);
    }
}
