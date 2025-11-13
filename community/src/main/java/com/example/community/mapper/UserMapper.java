package com.example.community.mapper;

import com.example.community.common.util.Times;
import com.example.community.domain.User;
import com.example.community.dto.user.UserResponse;

public final class UserMapper {
    private UserMapper() {}

    public static UserResponse toResponse(User u) {
        Long id = null;
        String email = "";
        String nickname = "";
        String profileImage = "";
        String createdAtStr = "unknown";
        String updatedAtStr = "unknown";

        if (u != null) {
            id = u.getId();

            String e = u.getEmail();
            if (e != null) {
                email = e;
            }
            String n = u.getNickname();
            if (n != null) {
                nickname = n;
            }
            String pi = u.getProfileImageUrl();
            if (pi != null) {
                profileImage = pi;
            }

            createdAtStr = Times.toIsoOrUnknown(u.getCreatedAt());
            updatedAtStr = Times.toIsoOrUnknown(u.getUpdatedAt());
        }

        return new UserResponse(
                id,
                email,
                nickname,
                profileImage,
                createdAtStr,
                updatedAtStr
        );
    }
}
