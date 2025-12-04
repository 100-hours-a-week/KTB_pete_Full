package com.example.community.service;

import com.example.community.common.BusinessException;
import com.example.community.common.ErrorCode;
import com.example.community.domain.RefreshToken;
import com.example.community.domain.User;
import com.example.community.repository.RefreshTokenRepository;
import com.example.community.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    // ===== issue =====

    @Test
    @DisplayName("issue: 유저가 존재하지 않으면 MEMBER_NOT_FOUND 에러를 던진다")
    void issue_shouldThrowWhenUserNotFound() {
        // given
        Long userId = 1L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> refreshTokenService.issue(userId)
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
        then(userRepository).should().findById(userId);
        then(refreshTokenRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("issue: 기존 토큰이 없으면 새 RefreshToken을 생성하여 저장한다")
    void issue_shouldCreateNewTokenWhenNoExistingToken() {
        // given
        Long userId = 1L;
        User user = User.create("email@example.com", "pw", "nick", null);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // 기존 토큰 없음
        given(refreshTokenRepository.findByUserId(userId)).willReturn(Optional.empty());

        // save 호출 시, 넘겨준 엔티티 그대로 반환하도록 설정
        given(refreshTokenRepository.save(any(RefreshToken.class)))
                .willAnswer(invocation -> invocation.getArgument(0, RefreshToken.class));

        // when
        RefreshTokenService.RefreshTokenResult result = refreshTokenService.issue(userId);

        // then
        then(userRepository).should().findById(userId);
        then(refreshTokenRepository).should().findByUserId(userId);
        then(refreshTokenRepository).should().save(any(RefreshToken.class));

        assertThat(result.userId).isEqualTo(userId);
        assertThat(result.token).isNotNull();
        assertThat(result.expiresAt).isAfter(Instant.now().minusSeconds(10)); // 대략적인 검증
    }

    @Test
    @DisplayName("issue: 기존 토큰이 있으면 해당 엔티티의 값을 새 토큰으로 교체한다")
    void issue_shouldUpdateExistingTokenWhenAlreadyExists() {
        // given
        Long userId = 1L;
        User user = User.create("email@example.com", "pw", "nick", null);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // 기존 토큰 엔티티
        Instant oldExpiresAt = Instant.now().minusSeconds(1000);
        RefreshToken existing = RefreshToken.create(userId, "old-token", oldExpiresAt);
        // id가 필요하면 여기서 리플렉션으로 세팅해도 됨

        given(refreshTokenRepository.findByUserId(userId)).willReturn(Optional.of(existing));

        // save 호출 시, 넘겨준 엔티티 그대로 반환하도록 설정
        given(refreshTokenRepository.save(any(RefreshToken.class)))
                .willAnswer(invocation -> invocation.getArgument(0, RefreshToken.class));

        String oldTokenValue = existing.getToken();

        // when
        RefreshTokenService.RefreshTokenResult result = refreshTokenService.issue(userId);

        // then
        then(userRepository).should().findById(userId);
        then(refreshTokenRepository).should().findByUserId(userId);
        then(refreshTokenRepository).should().save(existing);

        // 토큰 값이 바뀌었는지 (랜덤이라 정확한 값은 모름)
        assertThat(existing.getToken()).isNotEqualTo(oldTokenValue);
        assertThat(existing.getToken()).isEqualTo(result.token);

        // 만료 시간도 갱신되었는지
        assertThat(existing.getExpiresAt()).isAfter(oldExpiresAt);
        assertThat(existing.getExpiresAt()).isEqualTo(result.expiresAt);
    }

    // ===== validate =====

    @Test
    @DisplayName("validate: 토큰이 null이면 UNAUTHORIZED 에러를 던진다")
    void validate_shouldThrowWhenTokenIsNull() {
        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> refreshTokenService.validate(null)
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);
        then(refreshTokenRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("validate: 토큰이 공백이면 UNAUTHORIZED 에러를 던진다")
    void validate_shouldThrowWhenTokenIsBlank() {
        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> refreshTokenService.validate("   ")
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);
        then(refreshTokenRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("validate: 토큰을 찾을 수 없으면 UNAUTHORIZED 에러를 던진다")
    void validate_shouldThrowWhenTokenNotFound() {
        // given
        String tokenValue = "token";
        given(refreshTokenRepository.findByToken(tokenValue)).willReturn(Optional.empty());

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> refreshTokenService.validate(tokenValue)
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);
        then(refreshTokenRepository).should().findByToken(tokenValue);
    }

    @Test
    @DisplayName("validate: 토큰이 만료되었으면 삭제 후 UNAUTHORIZED 에러를 던진다")
    void validate_shouldDeleteTokenAndThrowWhenExpired() {
        // given
        String tokenValue = "token";

        RefreshToken token = RefreshToken.create(1L, tokenValue, Instant.now().minusSeconds(10));
        // isExpired(now) 가 true를 반환하도록 만든 엔티티라고 가정

        given(refreshTokenRepository.findByToken(tokenValue)).willReturn(Optional.of(token));

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> refreshTokenService.validate(tokenValue)
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED);
        then(refreshTokenRepository).should().findByToken(tokenValue);
        then(refreshTokenRepository).should().deleteById(token.getId());
    }

    @Test
    @DisplayName("validate: 유효한 토큰이면 userId, token, expiresAt 정보를 담아 반환한다")
    void validate_shouldReturnResultWhenTokenIsValid() {
        // given
        String tokenValue = "token";
        Instant expiresAt = Instant.now().plusSeconds(1000);
        RefreshToken token = RefreshToken.create(1L, tokenValue, expiresAt);

        given(refreshTokenRepository.findByToken(tokenValue)).willReturn(Optional.of(token));

        // when
        RefreshTokenService.RefreshTokenResult result = refreshTokenService.validate(tokenValue);

        // then
        then(refreshTokenRepository).should().findByToken(tokenValue);
        then(refreshTokenRepository).should(never()).deleteById(anyLong());

        assertThat(result.userId).isEqualTo(token.getUserId());
        assertThat(result.token).isEqualTo(token.getToken());
        assertThat(result.expiresAt).isEqualTo(token.getExpiresAt());
    }

    // ===== revokeByUserId =====

    @Test
    @DisplayName("revokeByUserId: 해당 유저의 모든 토큰을 삭제한다")
    void revokeByUserId_shouldDeleteTokensByUserId() {
        // given
        Long userId = 1L;

        // when
        refreshTokenService.revokeByUserId(userId);

        // then
        then(refreshTokenRepository).should().deleteByUserId(userId);
    }

    // ===== revokeByToken =====

    @Test
    @DisplayName("revokeByToken: 토큰이 null이면 아무 것도 하지 않는다")
    void revokeByToken_shouldDoNothingWhenTokenIsNull() {
        // when
        refreshTokenService.revokeByToken(null);

        // then
        then(refreshTokenRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("revokeByToken: 토큰이 공백이면 아무 것도 하지 않는다")
    void revokeByToken_shouldDoNothingWhenTokenIsBlank() {
        // when
        refreshTokenService.revokeByToken("   ");

        // then
        then(refreshTokenRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("revokeByToken: 토큰이 존재하면 deleteById를 호출한다")
    void revokeByToken_shouldDeleteTokenWhenExists() {
        // given
        String tokenValue = "token";
        RefreshToken token = RefreshToken.create(1L, tokenValue, Instant.now().plusSeconds(1000));

        given(refreshTokenRepository.findByToken(tokenValue)).willReturn(Optional.of(token));

        // when
        refreshTokenService.revokeByToken(tokenValue);

        // then
        then(refreshTokenRepository).should().findByToken(tokenValue);
        then(refreshTokenRepository).should().deleteById(token.getId());
    }

    @Test
    @DisplayName("revokeByToken: 토큰이 존재하지 않으면 아무 것도 하지 않는다")
    void revokeByToken_shouldDoNothingWhenTokenNotFound() {
        // given
        String tokenValue = "token";
        given(refreshTokenRepository.findByToken(tokenValue)).willReturn(Optional.empty());

        // when
        refreshTokenService.revokeByToken(tokenValue);

        // then
        then(refreshTokenRepository).should().findByToken(tokenValue);
        then(refreshTokenRepository).should(never()).deleteById(anyLong());
    }
}
