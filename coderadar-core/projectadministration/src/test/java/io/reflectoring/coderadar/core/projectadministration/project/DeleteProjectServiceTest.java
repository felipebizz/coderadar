package io.reflectoring.coderadar.core.projectadministration.project;

import io.reflectoring.coderadar.core.projectadministration.port.driven.project.DeleteProjectPort;
import io.reflectoring.coderadar.core.projectadministration.service.project.DeleteProjectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class DeleteProjectServiceTest {
  @Mock private DeleteProjectPort deleteProjectPort;
  @InjectMocks private DeleteProjectService testSubject;

  @Test
  void deleteProjectWithIdOne() {
    testSubject.delete(1L);

    Mockito.verify(deleteProjectPort, Mockito.times(1)).delete(1L);
  }
}
