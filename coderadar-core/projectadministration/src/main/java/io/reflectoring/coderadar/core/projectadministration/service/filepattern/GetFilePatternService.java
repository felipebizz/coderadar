package io.reflectoring.coderadar.core.projectadministration.service.filepattern;

import io.reflectoring.coderadar.core.projectadministration.domain.FilePattern;
import io.reflectoring.coderadar.core.projectadministration.port.driven.filepattern.GetFilePatternPort;
import io.reflectoring.coderadar.core.projectadministration.port.driver.filepattern.get.GetFilePatternResponse;
import io.reflectoring.coderadar.core.projectadministration.port.driver.filepattern.get.GetFilePatternUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service("GetFilePatternService")
public class GetFilePatternService implements GetFilePatternUseCase {
  private final GetFilePatternPort getFilePatternPort;

  @Autowired
  public GetFilePatternService(@Qualifier("GetFilePatternServiceNeo4j") GetFilePatternPort getFilePatternPort) {
    this.getFilePatternPort = getFilePatternPort;
  }

  @Override
  public GetFilePatternResponse get(Long id) {
    FilePattern filePattern = getFilePatternPort.get(id);
    return new GetFilePatternResponse(id, filePattern.getPattern(), filePattern.getInclusionType());
  }
}
