package com.example.community.mapper;

import com.example.community.common.util.Numbers;
import com.example.community.common.util.Strings;
import com.example.community.common.util.Times;
import com.example.community.domain.Post;
import com.example.community.domain.User;
import com.example.community.dto.post.PostResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class PostMapper {
    private PostMapper() {}

    // 단건 매핑: Post + userId → PostResponse
    public static PostResponse toResponse(Post p, Long userId) {
        Long idVal = null;
        String title = "";
        String content = "";
        String image = "";
        String comments = "0";
        String likes = "0";
        String views = "0";
        String createdAtStr = "unknown";
        String updatedAtStr = "unknown";

        if (p != null) {
            idVal = p.getId();

            String t = p.getTitle();
            if (t != null) {
                title = t;
            }
            String c = p.getContent();
            if (c != null) {
                content = c;
            }

            image = Strings.nullToEmpty(p.getImage());
            comments = Numbers.toStringOrZero(p.getComments());
            likes    = Numbers.toStringOrZero(p.getLikes());
            views    = Numbers.toStringOrZero(p.getViews());

            createdAtStr = Times.toIsoOrUnknown(p.getCreatedAt());
            updatedAtStr = Times.toIsoOrUnknown(p.getUpdatedAt());
        }

        return new PostResponse(
                idVal,
                Numbers.longOrZero(userId),
                title,
                content,
                image,
                comments,
                likes,
                views,
                createdAtStr,
                updatedAtStr
        );
    }

    // 목록 매핑: posts + authorMap → List<PostResponse>
    public static List<PostResponse> toResponseList(List<Post> posts, Map<Long, User> authorMap) {
        List<PostResponse> items = new ArrayList<>();
        if (posts == null) {
            return items;
        }
        int size = posts.size();
        int i = 0;
        while (i < size) {
            Post p = posts.get(i);
            Long authorId = null;
            if (p != null) {
                authorId = p.getAuthorId();
            }
            Long userId = 0L;
            if (authorId != null) {
                User au = authorMap.get(authorId);
                if (au != null && au.getId() != null) {
                    userId = au.getId();
                } else {
                    userId = authorId; // 맵에 없으면 원본 authorId 사용
                }
            }
            items.add(toResponse(p, userId));
            i = i + 1;
        }
        return items;
    }
}
