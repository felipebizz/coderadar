package io.reflectoring.coderadar.graph.projectadministration.user.service;

import io.reflectoring.coderadar.core.projectadministration.domain.RefreshToken;
import io.reflectoring.coderadar.core.projectadministration.domain.User;
import io.reflectoring.coderadar.core.projectadministration.port.driven.user.RefreshTokenPort;
import io.reflectoring.coderadar.graph.projectadministration.user.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("RefreshTokenServiceNeo4j")
public class RefreshTokenService implements RefreshTokenPort {

  private final RefreshTokenRepository refreshTokenRepository;

  @Autowired
  public RefreshTokenService(RefreshTokenRepository refreshTokenRepository) {
    this.refreshTokenRepository = refreshTokenRepository;
  }

  @Override
  public RefreshToken findByToken(String refreshToken) {
    return refreshTokenRepository.findByToken(refreshToken);
  }

  @Override
  @Transactional
  public void deleteByUser(User user) {
    refreshTokenRepository.deleteByUser(user);
  }

  @Override
  public void updateRefreshToken(String oldToken, String newToken) {
    RefreshToken refreshToken = findByToken(oldToken);
    refreshToken.setToken(newToken);
    refreshTokenRepository.save(refreshToken);
  }

  @Override
  public void saveToken(RefreshToken refreshTokenEntity) {
    refreshTokenRepository.save(refreshTokenEntity);
  }
}
