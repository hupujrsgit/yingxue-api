package com.exeplm.dto;

import lombok.Data;

import java.util.Date;

@Data
public class VideoDto {
    private Integer id;
    private String title;
    private String cover;
    /**
     * 分类名
     */
    private String category;
    /**
     * 点赞数
     */
    private Integer likes;
    /**
     * up主
     */
    private String uploader;
    /**
     * 创建时间
     */
    private Date createdAt;
}
