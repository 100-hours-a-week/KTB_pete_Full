package com.example.community.service;

import com.example.community.common.BusinessException;
import com.example.community.common.ErrorCode;
import com.example.community.domain.User;
import com.example.community.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    // ===== findByIds =====

    @Test
    @DisplayName("findByIds: ids가 null이면 DB를 조회하지 않고 빈 Map을 반환한다")
    void findByIds_shouldReturnEmptyMap_whenIdsIsNull() {
        // given
        Set<Long> ids = null;

        // when
        Map<Long, User> result = userService.findByIds(ids);

        // then
        assertThat(result).isEmpty();
        then(userRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("findByIds: ids가 비어 있으면 DB를 조회하지 않고 빈 Map을 반환한다")
    void findByIds_shouldReturnEmptyMap_whenIdsIsEmpty() {
        // given
        Set<Long> ids = Collections.emptySet();

        // when
        Map<Long, User> result = userService.findByIds(ids);

        // then
        assertThat(result).isEmpty();
        then(userRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("findByIds: 존재하는 유저만 id를 key로 하는 Map에 담아 반환한다")
    void findByIds_shouldReturnFoundUsersMappedById() throws Exception {
        // given
        Set<Long> ids = new HashSet<>(Arrays.asList(1L, 2L, 3L));

        User u1 = User.create("u1@example.com", "pw", "u1", null);
        User u3 = User.create("u3@example.com", "pw", "u3", null);

        // 테스트 편의를 위해 리플렉션으로 id 필드 세팅
        setUserId(u1, 1L);
        setUserId(u3, 3L);

        given(userRepository.findAllById(ids)).willReturn(Arrays.asList(u1, u3));

        // when
        Map<Long, User> result = userService.findByIds(ids);

        // then
        then(userRepository).should().findAllById(ids);
        assertThat(result)
                .hasSize(2)
                .containsEntry(1L, u1)
                .containsEntry(3L, u3);
    }

    // ===== signup =====

    @Test
    @DisplayName("signup: 이메일이 이미 존재하면 EMAIL_ALREADY_EXIST 에러를 던지고 저장하지 않는다")
    void signup_shouldThrowWhenEmailAlreadyExists() {
        // given
        String email = "dup@example.com";
        given(userRepository.findByEmail(email))
                .willReturn(Optional.of(User.create(email, "pw", "nick", null)));

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> userService.signup(email, "pw", "nick", null)
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.EMAIL_ALREADY_EXIST);
        then(userRepository).should().findByEmail(email);
        then(userRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("signup: 이메일이 존재하지 않으면 새 User를 생성해서 저장하고 반환한다")
    void signup_shouldCreateUserWhenEmailNotExists() {
        // given
        String email = "new@example.com";
        String pw = "pw";
        String nickname = "nick";
        String profile = "img.png";

        given(userRepository.findByEmail(email)).willReturn(Optional.empty());
        given(userRepository.save(any(User.class)))
                .willAnswer(invocation -> invocation.getArgument(0, User.class));

        // when
        User result = userService.signup(email, pw, nickname, profile);

        // then
        then(userRepository).should().findByEmail(email);
        then(userRepository).should().save(any(User.class));

        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getPassword()).isEqualTo(pw);
        assertThat(result.getNickname()).isEqualTo(nickname);
        assertThat(result.getProfileImageUrl()).isEqualTo(profile);
    }

    // ===== login =====

    @Test
    @DisplayName("login: 이메일이 존재하지 않으면 MEMBER_NOT_FOUND 에러를 던진다")
    void login_shouldThrowWhenUserNotFound() {
        // given
        String email = "nope@example.com";
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> userService.login(email, "pw")
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
        then(userRepository).should().findByEmail(email);
    }

    @Test
    @DisplayName("login: 비밀번호가 다르면 LOGIN_PASSWORD_WRONG 에러를 던진다")
    void login_shouldThrowWhenPasswordIsWrong() {
        // given
        String email = "user@example.com";
        User user = User.create(email, "savedPw", "nick", null);
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> userService.login(email, "wrongPw")
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.LOGIN_PASSWORD_WRONG);
        then(userRepository).should().findByEmail(email);
    }

    @Test
    @DisplayName("login: 이메일과 비밀번호가 일치하면 User를 반환한다")
    void login_shouldReturnUserWhenEmailAndPasswordMatch() {
        // given
        String email = "user@example.com";
        User user = User.create(email, "pw", "nick", null);
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        // when
        User result = userService.login(email, "pw");

        // then
        assertThat(result).isSameAs(user);
        then(userRepository).should().findByEmail(email);
    }

    // ===== getMe =====

    @Test
    @DisplayName("getMe: userId에 해당하는 유저가 존재하면 반환한다")
    void getMe_shouldReturnUserWhenExists() {
        // given
        Long userId = 1L;
        User user = User.create("email@example.com", "pw", "nick", null);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        User result = userService.getMe(userId);

        // then
        assertThat(result).isSameAs(user);
        then(userRepository).should().findById(userId);
    }

    @Test
    @DisplayName("getMe: 유저가 존재하지 않으면 MEMBER_NOT_FOUND 에러를 던진다")
    void getMe_shouldThrowWhenUserNotFound() {
        // given
        Long userId = 1L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> userService.getMe(userId)
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
        then(userRepository).should().findById(userId);
    }

    // ===== updateMe =====

    @Test
    @DisplayName("updateMe: 닉네임과 프로필 이미지를 업데이트하고 updatedAt을 변경한다")
    void updateMe_shouldUpdateProfileAndUpdatedAt() {
        // given
        Long userId = 1L;
        User user = User.create("email@example.com", "pw", "oldNick", "old.png");
        Instant before = user.getUpdatedAt();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(userRepository.save(any(User.class)))
                .willAnswer(invocation -> invocation.getArgument(0, User.class));

        // when
        User result = userService.updateMe(userId, "newNick", "new.png");

        // then
        then(userRepository).should().findById(userId);
        then(userRepository).should().save(user);

        assertThat(result.getNickname()).isEqualTo("newNick");
        assertThat(result.getProfileImageUrl()).isEqualTo("new.png");
        assertThat(result.getUpdatedAt()).isAfterOrEqualTo(before);
    }

    @Test
    @DisplayName("updateMe: 유저가 존재하지 않으면 MEMBER_NOT_FOUND 에러를 던진다")
    void updateMe_shouldThrowWhenUserNotFound() {
        // given
        Long userId = 1L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> userService.updateMe(userId, "newNick", "new.png")
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
        then(userRepository).should().findById(userId);
        then(userRepository).should(never()).save(any());
    }

    // ===== updatePassword =====

    @Test
    @DisplayName("updatePassword: 새 비밀번호가 null이면 BAD_REQUEST 에러를 던진다")
    void updatePassword_shouldThrowWhenNewPasswordIsNull() {
        // given
        Long userId = 1L;

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> userService.updatePassword(userId, null)
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.BAD_REQUEST);
        then(userRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("updatePassword: 유저가 존재하지 않으면 MEMBER_NOT_FOUND 에러를 던진다")
    void updatePassword_shouldThrowWhenUserNotFound() {
        // given
        Long userId = 1L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> userService.updatePassword(userId, "newPw")
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
        then(userRepository).should().findById(userId);
        then(userRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("updatePassword: 비밀번호와 updatedAt을 변경하고 저장한다")
    void updatePassword_shouldChangePasswordAndSaveUser() {
        // given
        Long userId = 1L;
        User user = User.create("email@example.com", "oldPw", "nick", null);
        Instant before = user.getUpdatedAt();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        userService.updatePassword(userId, "newPw");

        // then
        then(userRepository).should().findById(userId);
        then(userRepository).should().save(user);

        assertThat(user.getPassword()).isEqualTo("newPw");
        assertThat(user.getUpdatedAt()).isAfterOrEqualTo(before);
    }

    // ===== withdraw =====

    @Test
    @DisplayName("withdraw: 유저가 존재하면 deleteById를 호출한다")
    void withdraw_shouldDeleteUserWhenExists() {
        // given
        Long userId = 1L;
        User user = User.create("email@example.com", "pw", "nick", null);
        given(userRepository.findById(userId)).willReturn(Optional.of(user));

        // when
        userService.withdraw(userId);

        // then
        then(userRepository).should().findById(userId);
        then(userRepository).should().deleteById(userId);
    }

    @Test
    @DisplayName("withdraw: 유저가 존재하지 않으면 MEMBER_NOT_FOUND 에러를 던지고 deleteById는 호출하지 않는다")
    void withdraw_shouldThrowWhenUserNotFound() {
        // given
        Long userId = 1L;
        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when
        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> userService.withdraw(userId)
        );

        // then
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.MEMBER_NOT_FOUND);
        then(userRepository).should().findById(userId);
        then(userRepository).should(never()).deleteById(anyLong());
    }

    // ===== 테스트용 리플렉션 헬퍼 =====

    /**
     * JPA 엔티티의 id 필드는 private 이고 setter가 없으므로,
     * 테스트에서만 리플렉션으로 값을 주입해서 사용합니다.
     * (실제 코드에는 영향이 없습니다)
     */
    private void setUserId(User user, Long id) throws Exception {
        var field = User.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(user, id);
    }
}
