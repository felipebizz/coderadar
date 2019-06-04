package io.reflectoring.coderadar.graph.query.service;

import io.reflectoring.coderadar.core.projectadministration.ProjectNotFoundException;
import io.reflectoring.coderadar.core.projectadministration.domain.Project;
import io.reflectoring.coderadar.core.query.port.driven.GetAvailableMetricsInProjectPort;
import io.reflectoring.coderadar.graph.projectadministration.project.repository.GetProjectRepository;
import io.reflectoring.coderadar.graph.query.repository.GetAvailableMetricsInProjectRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("GetAvailableMetricsInProjectServiceNeo4j")
public class GetAvailableMetricsInProjectService implements GetAvailableMetricsInProjectPort {
  private final GetProjectRepository getProjectRepository;
  private final GetAvailableMetricsInProjectRepository getAvailableMetricsInProjectRepository;

  @Autowired
  public GetAvailableMetricsInProjectService(
      GetProjectRepository getProjectRepository,
      GetAvailableMetricsInProjectRepository getAvailableMetricsInProjectRepository) {
    this.getProjectRepository = getProjectRepository;
    this.getAvailableMetricsInProjectRepository = getAvailableMetricsInProjectRepository;
  }

  @Override
  public List<String> get(Long projectId) {
    Optional<Project> persistedProject = getProjectRepository.findById(projectId);

    if (persistedProject.isPresent()) {
      return getAvailableMetricsInProjectRepository.getAvailableMetricsInProject(projectId);
    } else {
      throw new ProjectNotFoundException(
          String.format("A project with the ID '%d' doesn't exists.", projectId));
    }
  }
}
