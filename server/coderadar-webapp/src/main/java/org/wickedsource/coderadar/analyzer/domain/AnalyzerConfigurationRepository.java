package org.wickedsource.coderadar.analyzer.domain;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AnalyzerConfigurationRepository extends CrudRepository<AnalyzerConfiguration, Long> {

    AnalyzerConfiguration findByProjectIdAndAnalyzerName(Long projectId, String analyzerName);

    List<AnalyzerConfiguration> findByProjectId(Long projectId);

    AnalyzerConfiguration deleteByProjectIdAndAnalyzerName(Long projectId, String analyzerName);

    AnalyzerConfiguration findByProjectIdAndId(Long projectId, Long analyzerConfigurationId);
}