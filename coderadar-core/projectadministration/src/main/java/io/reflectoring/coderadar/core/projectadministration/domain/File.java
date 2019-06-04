package io.reflectoring.coderadar.core.projectadministration.domain;

import java.util.List;
import lombok.Data;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

/** Represents a file in a VCS repository. */
@NodeEntity
@Data
public class File {
  private Long id;
  private String path;

  @Relationship private List<MetricValue> metricValues;

  @Relationship(type = "HAS_RENAMED") private File hasRenamed;
}
