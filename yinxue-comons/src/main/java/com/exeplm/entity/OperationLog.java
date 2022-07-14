package com.exeplm.entity;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class OperationLog {
    private String id;
    private Long sysUserId;
    private String ip;
    private String type;
    private String desciption;
    private String model;
    private String operationTime;
    private String result;
}


