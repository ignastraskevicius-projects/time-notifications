package org.ignast.challenge.timenotifications.domain;

import static org.assertj.core.api.Assertions.assertThat;

import lombok.val;
import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.jupiter.api.Test;

class TimestampTest {

    @Test
    public void shouldBeEqual() {
        EqualsVerifier.forClass(Timestamp.class).verify();
    }

    @Test
    public void shouldBeRepresentableAsString() {
        assertThat(Timestamp.current().toString()).startsWith("Timestamp(valueInSeconds=1");
    }

    @Test
    public void shouldPreserveTimestampInSeconds() {
        val currentTimeSeconds = (int) (System.currentTimeMillis() / 1000l);

        val timestamp = Timestamp.current();

        assertThat(timestamp.getValueInSeconds()).isGreaterThanOrEqualTo(currentTimeSeconds);
        assertThat(timestamp.getValueInSeconds()).isLessThanOrEqualTo(currentTimeSeconds + 1);
    }

    @Test
    public void shouldProvideTimestampInTheFuture() {
        val now = Timestamp.current();
        val oneSecondInTheFuture = now.plusSeconds(1);

        assertThat(now.getValueInSeconds() + 1).isEqualTo(oneSecondInTheFuture.getValueInSeconds());
    }

    @Test
    public void timestampInTheFutureShouldBeConsideredGreaterOrEqual() {
        val now = Timestamp.current();
        val oneSecondInTheFuture = now.plusSeconds(1);

        assertThat(oneSecondInTheFuture.gte(now)).isTrue();
    }

    @Test
    public void timestampInThePastShouldBeConsideredGreaterOrEqual() {
        val now = Timestamp.current();
        val oneSecondInTheFuture = now.plusSeconds(1);

        assertThat(now.gte(oneSecondInTheFuture)).isFalse();
    }

    @Test
    public void timestampShouldBeConsideredGreaterOrEqualToItself() {
        val now = Timestamp.current();

        assertThat(now.gte(now)).isTrue();
    }
}
