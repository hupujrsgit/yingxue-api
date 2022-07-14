package com.exeplm.dto;

import com.exeplm.entity.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class CommentDto {
    private Integer id;
    /**
     * 评论内容
     */
    private String content;
    /**
     * 父评论id
     */
    private Integer parentId;

    private Date createdAt;
    /**
     * 评论者
     */
    private UserDto reviewer;
    /**
     * 子评论
     */
    @JsonProperty("sub_comments")
    private List<CommentDto> subComments;
}
