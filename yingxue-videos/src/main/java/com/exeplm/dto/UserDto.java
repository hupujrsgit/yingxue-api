package com.exeplm.dto;

import lombok.Data;

@Data
public class UserDto {
    private Integer id;
    /**
     * 用户名
     */
    private String name;
    /**
     * 头像链接
     */
    private String avatar;
}
