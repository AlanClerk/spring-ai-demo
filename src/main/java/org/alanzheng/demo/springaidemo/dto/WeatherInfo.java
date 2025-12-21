package org.alanzheng.demo.springaidemo.dto;

/**
 * 天气信息的结构化输出DTO
 * 用于演示Spring AI的结构化输出和Advisors API功能
 */
public record WeatherInfo(
        /**
         * 城市名称
         */
        String city,
        /**
         * 温度（摄氏度）
         */
        Double temperature,
        /**
         * 天气描述
         */
        String description
) {
}

