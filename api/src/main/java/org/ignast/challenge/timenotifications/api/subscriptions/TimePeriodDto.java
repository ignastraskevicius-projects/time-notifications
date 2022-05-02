package org.ignast.challenge.timenotifications.api.subscriptions;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

public record TimePeriodDto(
    @NotNull @Min(4) @Max(4 * 60 * 60) Integer amount,
    @NotNull @Pattern(regexp = "second") String timeUnit
) {}
