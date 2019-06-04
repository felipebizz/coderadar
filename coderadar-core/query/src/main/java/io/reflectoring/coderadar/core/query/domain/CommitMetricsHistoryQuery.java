package io.reflectoring.coderadar.core.query.domain;

import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Provides parameters to query for a history of values for a specific metric. */
@Data
@NoArgsConstructor
public class CommitMetricsHistoryQuery {

  @NotNull private String metric;

  @NotNull private DateRange dateRange;

  @NotNull private Interval interval;
}
