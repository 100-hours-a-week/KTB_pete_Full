// mapper/PostMapper.java
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

    // 단건 매핑
    public static PostResponse toResponse(Post p, User author, boolean liked) {
        Long idVal = null;
        Long userId = 0L;
        String writerNickname = "";
        String writerProfileImage = "";
        String title = "";
        String content = "";
        String image = "";
        String comments = "0";
        String likes = "0";
        String views = "0";
        String likedStr = "false";
        String createdAtStr = "unknown";
        String updatedAtStr = "unknown";

        if (p != null) {
            idVal = p.getId();
            Long aid = p.getAuthorId();
            if (aid != null) {
                userId = aid;
            }

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

        if (author != null) {
            String nick = author.getNickname();
            if (nick != null) {
                writerNickname = nick;
            }
            String pi = author.getProfileImageUrl();
            if (pi != null) {
                writerProfileImage = pi;
            }
        }

        if (liked) {
            likedStr = "true";
        }

        return new PostResponse(
                idVal,
                userId,
                writerNickname,
                writerProfileImage,
                title,
                content,
                image,
                comments,
                likes,
                views,
                likedStr,
                createdAtStr,
                updatedAtStr
        );
    }

    // 목록 매핑
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
            User author = null;

            if (p != null) {
                authorId = p.getAuthorId();
            }
            if (authorId != null) {
                User au = authorMap.get(authorId);
                if (au != null && au.getId() != null) {
                    author = au;
                }
            }
            // 목록에서는 liked 정보를 사용하지 않으므로 항상 false
            items.add(toResponse(p, author, false));
            i = i + 1;
        }
        return items;
    }
}
