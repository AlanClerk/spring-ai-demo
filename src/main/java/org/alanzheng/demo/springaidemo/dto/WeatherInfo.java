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
         * 月份（1-12）
         */
        Integer month,
        /**
         * 平均温度（摄氏度）
         */
        Double averageTemperature,
        /**
         * 天气描述
         */
        String description
) {
}

