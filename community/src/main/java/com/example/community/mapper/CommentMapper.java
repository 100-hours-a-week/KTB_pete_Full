package com.example.community.mapper;

import com.example.community.common.util.Numbers;
import com.example.community.common.util.Times;
import com.example.community.domain.Comment;
import com.example.community.domain.User;
import com.example.community.dto.comment.CommentResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class CommentMapper {
    private CommentMapper() {}

    // 단건 매핑
    public static CommentResponse toResponse(Comment c, User author, long likeCount) {
        Long idVal = null;
        Long postIdVal = null;
        Long userId = 0L;
        String writerNickname = "";
        String writerProfileImage = "";
        String content = "";
        String likesStr = Numbers.toStringOrZero(likeCount);
        String createdAtStr = "unknown";
        String updatedAtStr = "unknown";

        if (c != null) {
            idVal = c.getId();
            postIdVal = c.getPostId();

            Long aid = c.getAuthorId();
            if (aid != null) {
                userId = aid;
            }

            String txt = c.getContent();
            if (txt != null) {
                content = txt;
            }

            createdAtStr = Times.toIsoOrUnknown(c.getCreatedAt());
            updatedAtStr = Times.toIsoOrUnknown(c.getUpdatedAt());
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

        return new CommentResponse(
                idVal,
                postIdVal,
                userId,
                writerNickname,
                writerProfileImage,
                content,
                likesStr,
                createdAtStr,
                updatedAtStr
        );
    }

    // 목록 매핑
    public static List<CommentResponse> toResponseList(
            List<Comment> comments,
            Map<Long, User> authorMap,
            Map<Long, Long> likeCountMap
    ) {
        List<CommentResponse> items = new ArrayList<CommentResponse>();
        if (comments == null) {
            return items;
        }
        int size = comments.size();
        int i = 0;
        while (i < size) {
            Comment c = comments.get(i);
            Long authorId = null;
            User author = null;

            if (c != null) {
                authorId = c.getAuthorId();
            }
            if (authorId != null) {
                User au = authorMap.get(authorId);
                if (au != null && au.getId() != null) {
                    author = au;
                }
            }

            long likeCount = 0L;
            if (c != null) {
                Long cid = c.getId();
                if (cid != null) {
                    Long cnt = likeCountMap.get(cid);
                    if (cnt != null) {
                        likeCount = cnt.longValue();
                    }
                }
            }

            items.add(toResponse(c, author, likeCount));
            i = i + 1;
        }
        return items;
    }
}
