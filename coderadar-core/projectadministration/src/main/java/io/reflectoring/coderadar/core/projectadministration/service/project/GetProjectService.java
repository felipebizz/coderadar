package io.reflectoring.coderadar.core.projectadministration.service.project;

import io.reflectoring.coderadar.core.projectadministration.ProjectNotFoundException;
import io.reflectoring.coderadar.core.projectadministration.domain.Project;
import io.reflectoring.coderadar.core.projectadministration.port.driven.project.GetProjectPort;
import io.reflectoring.coderadar.core.projectadministration.port.driver.project.get.GetProjectResponse;
import io.reflectoring.coderadar.core.projectadministration.port.driver.project.get.GetProjectUseCase;
import org.springframework.beans.factory.annotation.Qualifier;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service("GetProjectService")
public class GetProjectService implements GetProjectUseCase {

  private final GetProjectPort port;

  public GetProjectService(@Qualifier("GetProjectServiceNeo4j") GetProjectPort port) {
    this.port = port;
  }

  @Override
  public GetProjectResponse get(Long id) {
    Optional<Project> project = port.get(id);

    if (project.isPresent()) {
      Project persistedProject = project.get();
      GetProjectResponse response = new GetProjectResponse();
      response.setId(persistedProject.getId());
      response.setName(persistedProject.getName());
      response.setVcsUsername(persistedProject.getVcsUsername());
      response.setVcsPassword(persistedProject.getVcsPassword());
      response.setVcsOnline(persistedProject.isVcsOnline());
      response.setVcsUrl(persistedProject.getVcsUrl());
      response.setStart(persistedProject.getVcsStart());
      response.setEnd(persistedProject.getVcsEnd());
      return response;
    } else {
      throw new ProjectNotFoundException(id);
    }
  }
}
