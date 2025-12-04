package com.example.community.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    @DisplayName("create() 호출 시 id는 null이고, 필드와 타임스탬프가 올바르게 초기화된다")
    void create_shouldInitializeFieldsAndTimestamps() {
        // given
        String email = "test@example.com";
        String password = "pw";
        String nickname = "닉네임";
        String profile = "http://image.png";

        // when
        User user = User.create(email, password, nickname, profile);

        // then
        assertThat(user.getId()).isNull();
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getPassword()).isEqualTo(password);
        assertThat(user.getNickname()).isEqualTo(nickname);
        assertThat(user.getProfileImageUrl()).isEqualTo(profile);
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("닉네임과 프로필 이미지가 유효한 값이면 둘 다 trim 후 업데이트된다")
    void updateProfile_shouldUpdateBothNicknameAndProfileImage_whenValidValues() {
        // given
        User user = User.create("test@example.com", "pw", "oldNick", "old.png");

        // when
        user.updateProfile("  newNick  ", "  new.png  ");

        // then
        assertThat(user.getNickname()).isEqualTo("newNick");
        assertThat(user.getProfileImageUrl()).isEqualTo("new.png");
    }

    @Test
    @DisplayName("닉네임이 null 또는 공백이면 닉네임은 유지되고, 프로필 이미지만 변경된다")
    void updateProfile_shouldIgnoreBlankNickname_butUpdateProfileImage() {
        // given
        User user = User.create("test@example.com", "pw", "oldNick", "old.png");

        // when
        user.updateProfile("   ", "new.png");

        // then
        assertThat(user.getNickname()).isEqualTo("oldNick");
        assertThat(user.getProfileImageUrl()).isEqualTo("new.png");
    }

    @Test
    @DisplayName("프로필 이미지가 null 또는 공백이면 이미지는 유지되고, 닉네임만 변경된다")
    void updateProfile_shouldIgnoreBlankProfileImage_butUpdateNickname() {
        // given
        User user = User.create("test@example.com", "pw", "oldNick", "old.png");

        // when
        user.updateProfile("newNick", "   ");

        // then
        assertThat(user.getNickname()).isEqualTo("newNick");
        assertThat(user.getProfileImageUrl()).isEqualTo("old.png");
    }

    @Test
    @DisplayName("닉네임과 프로필 이미지가 모두 null/공백이면 아무 것도 변경되지 않는다")
    void updateProfile_shouldDoNothing_whenNicknameAndProfileImageBothNullOrBlank() {
        // given
        User user = User.create("test@example.com", "pw", "oldNick", "old.png");

        // when
        user.updateProfile("   ", null);

        // then
        assertThat(user.getNickname()).isEqualTo("oldNick");
        assertThat(user.getProfileImageUrl()).isEqualTo("old.png");
    }

    @Test
    @DisplayName("changePassword() 호출 시 비밀번호가 새 값으로 변경된다")
    void changePassword_shouldUpdatePasswordField() {
        // given
        User user = User.create("test@example.com", "oldPw", "nick", "img.png");

        // when
        user.changePassword("newPw");

        // then
        assertThat(user.getPassword()).isEqualTo("newPw");
    }

    @Test
    @DisplayName("touchUpdatedAt() 호출 시 updatedAt 필드가 주어진 값으로 변경된다")
    void touchUpdatedAt_shouldUpdateTimestamp() {
        // given
        User user = User.create("test@example.com", "pw", "nick", "img.png");
        Instant before = user.getUpdatedAt();
        Instant newTime = before.plusSeconds(100);

        // when
        user.touchUpdatedAt(newTime);

        // then
        assertThat(user.getUpdatedAt()).isEqualTo(newTime);
    }
}
