package com.exeplm.dto;

import com.exeplm.entity.User;
import lombok.Data;

import java.util.Date;
@Data
public class VideoDetailsDto {
    /**
     * up主信息
     */
    private User user;

    private String id;
    private String title;
    /**
     * 分类名称
     */
    private String category;
    private String link;
    private Date  createdAt;
    private Date  updateAt;
    /**
     * 播放次数
     */
    private Integer playsCount;
    /**
     * 喜欢次数
     */
    private Integer likesCount;
    /**
     * 是否喜欢
     */
    private boolean liked;
    /**
     * 是否不喜欢
     */
    private boolean disliked;
    /**
     * 是否收藏
     */
    private boolean favorite;
}
