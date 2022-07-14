package com.exeplm.dto;

import lombok.Data;

import java.util.List;
@Data
public class CategoryDto {
    private String id;
    private String name;
    private String parentId;
    private List<CategoryDto> children;
}
