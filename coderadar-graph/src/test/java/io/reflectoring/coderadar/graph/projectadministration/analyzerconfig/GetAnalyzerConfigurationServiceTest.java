package io.reflectoring.coderadar.graph.projectadministration.analyzerconfig;

import io.reflectoring.coderadar.core.projectadministration.domain.AnalyzerConfiguration;
import io.reflectoring.coderadar.graph.projectadministration.analyzerconfig.repository.GetAnalyzerConfigurationRepository;
import io.reflectoring.coderadar.graph.projectadministration.analyzerconfig.service.GetAnalyzerConfigurationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class GetAnalyzerConfigurationServiceTest {
    @Mock
    private GetAnalyzerConfigurationRepository getAnalyzerConfigurationRepository;

    @InjectMocks
    private GetAnalyzerConfigurationService getAnalyzerConfigurationService;

    @Test
    void withAnalyzerConfigurationIdShouldReturnAnalyzerConfigurationEntityAsOptional() {
        AnalyzerConfiguration mockedItem = new AnalyzerConfiguration();
        mockedItem.setId(1L);
        when(getAnalyzerConfigurationRepository.findById(any(Long.class))).thenReturn(Optional.of(mockedItem));

        Optional<AnalyzerConfiguration> returned = getAnalyzerConfigurationService.getAnalyzerConfiguration(1L);

        verify(getAnalyzerConfigurationRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(getAnalyzerConfigurationRepository);
        Assertions.assertTrue(returned.isPresent());
        Assertions.assertEquals(new Long(1L), returned.get().getId());
    }

    @Test
    void withNoPersistedAnalyzerConfigurationShouldReturnEmptyOptional() {
        Optional<AnalyzerConfiguration> mockedItem = Optional.empty();
        when(getAnalyzerConfigurationRepository.findById(any(Long.class))).thenReturn(mockedItem);

        Optional<AnalyzerConfiguration> returned = getAnalyzerConfigurationService.getAnalyzerConfiguration(1L);

        verify(getAnalyzerConfigurationRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(getAnalyzerConfigurationRepository);
        Assertions.assertFalse(returned.isPresent());
    }
}
