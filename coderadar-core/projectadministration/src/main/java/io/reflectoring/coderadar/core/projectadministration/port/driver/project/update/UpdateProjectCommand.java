package io.reflectoring.coderadar.core.projectadministration.port.driver.project.update;

import java.net.URL;
import java.util.Date;
import javax.validation.constraints.NotNull;
import lombok.Value;

@Value
public class UpdateProjectCommand {
  @NotNull private String name;
  @NotNull private String vcsUsername;
  @NotNull private String vcsPassword;
  @NotNull private URL vcsUrl;
  @NotNull private Boolean vcsOnline;
  @NotNull private Date start;
  @NotNull private Date end;
}
