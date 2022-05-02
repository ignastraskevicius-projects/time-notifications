package org.ignast.challenge.timenotifications.domain;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
@ToString
final class Timestamp {

    private final int valueInSeconds;

    static Timestamp current() {
        return new Timestamp((int) (System.currentTimeMillis() / 1000l));
    }

    public Timestamp plusSeconds(final int secondsToAdd) {
        return new Timestamp(valueInSeconds + secondsToAdd);
    }

    public boolean gte(final Timestamp other) {
        return valueInSeconds >= other.getValueInSeconds();
    }
}
