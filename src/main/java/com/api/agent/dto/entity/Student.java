package com.api.agent.dto.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Student {
    private Long id;
    private String name;
    private Integer gender; // 0:未知, 1:男, 2:女
    private Integer age;
    private String className;
    private Date createTime;
    private Date updateTime;
}