package org.alanzheng.demo.springaidemo.dto;

import lombok.Data;

import java.util.List;

/**
 * 演员及其电影列表的结构化输出DTO
 * 用于演示Spring AI的结构化输出功能
 */
@Data
public class ActorsFilms {
    /**
     * 演员姓名
     */
    private String actor;
    /**
     * 电影列表
     */
    private List<String> movies;
}

