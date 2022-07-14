package com.exeplm.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CommentsVo {
    private String content;
    @JsonProperty("parent_id")
    private Integer parentId;
}
