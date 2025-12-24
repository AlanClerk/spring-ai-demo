package org.alanzheng.demo.springaidemo.service;

import lombok.extern.slf4j.Slf4j;
import org.alanzheng.demo.springaidemo.dto.ActorsFilms;
import org.alanzheng.demo.springaidemo.dto.WeatherInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.StructuredOutputValidationAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 结构化输出服务
 * 演示Spring AI的结构化输出和Advisors API功能
 */
@Slf4j
@Service
public class StructuredOutputService {
    
    private static final int MAX_LOG_MESSAGE_LENGTH = 200;
    private static final int DEFAULT_MAX_RETRY_ATTEMPTS = 3;
    
    private final ChatModel chatModel;
    
    /**
     * 构造函数注入ChatModel
     */
    public StructuredOutputService(@Qualifier("openAiChatModel") ChatModel chatModel) {
        Objects.requireNonNull(chatModel, "ChatModel不能为空");
        this.chatModel = chatModel;
    }
    
    /**
     * 获取演员及其电影列表（基础结构化输出示例）
     * 
     * @param actorName 演员姓名
     * @param movieCount 电影数量
     * @return 演员及其电影列表
     */
    public ActorsFilms getActorsFilms(String actorName, int movieCount) {
        long startTime = System.currentTimeMillis();
        log.info("开始获取演员电影列表，演员: {}，数量: {}", actorName, movieCount);
        
        try {
            if (StringUtils.isBlank(actorName)) {
                throw new IllegalArgumentException("演员姓名不能为空");
            }
            if (movieCount <= 0) {
                throw new IllegalArgumentException("电影数量必须大于0");
            }
            
            // 使用ChatClient进行结构化输出
            ChatClient chatClient = ChatClient.builder(chatModel).build();
            
            String prompt = String.format("列出%s主演的%d部电影", actorName, movieCount);
            
            ActorsFilms result = chatClient.prompt()
                    .user(prompt)
                    .call()
                    /// 提前定义了一个public record ActorsFilms对象
                    /// 添加.entity()能够做到【同步调用】后的【相应转换】
                    .entity(ActorsFilms.class);  // 直接映射到POJO
            
            // 判空处理
            if (Objects.isNull(result)) {
                throw new RuntimeException("获取演员电影列表失败，返回结果为null");
            }
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("获取演员电影列表成功，耗时: {}ms，演员: {}，电影数量: {}", 
                    duration, result.actor(), 
                    Objects.nonNull(result.movies()) ? result.movies().size() : 0);
            
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("获取演员电影列表失败，耗时: {}ms，演员: {}，错误信息: {}", 
                    duration, actorName, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * 获取天气信息（使用Advisors API的结构化输出示例）
     * 使用StructuredOutputValidationAdvisor确保输出的有效性，自动重试
     * 
     * @param city 城市名称
     * @param month 月份（1-12）
     * @return 天气信息
     */
    public WeatherInfo getWeatherInfo(String city, int month) {
        return getWeatherInfo(city, month, DEFAULT_MAX_RETRY_ATTEMPTS);
    }
    
    /**
     * 获取天气信息（使用Advisors API的结构化输出示例）
     * 使用StructuredOutputValidationAdvisor确保输出的有效性，自动重试
     * 
     * @param city 城市名称
     * @param month 月份（1-12）
     * @param maxRetryAttempts 最大重试次数
     * @return 天气信息
     */
    public WeatherInfo getWeatherInfo(String city, int month, int maxRetryAttempts) {
        long startTime = System.currentTimeMillis();
        log.info("开始获取天气信息，城市: {}，月份: {}，最大重试次数: {}", city, month, maxRetryAttempts);
        
        try {
            if (StringUtils.isBlank(city)) {
                throw new IllegalArgumentException("城市名称不能为空");
            }
            if (month < 1 || month > 12) {
                throw new IllegalArgumentException("月份必须在1-12之间");
            }
            if (maxRetryAttempts < 1) {
                throw new IllegalArgumentException("最大重试次数必须大于0");
            }
            
            /// 创建验证Advisor，用于确保结构化输出的有效性
            StructuredOutputValidationAdvisor validationAdvisor = 
                    StructuredOutputValidationAdvisor.builder()
                    .outputType(WeatherInfo.class)          // 指定目标类型
                    .maxRepeatAttempts(maxRetryAttempts)    // 最多重试次数
                    .build();
            
            /// 构建ChatClient并注入Advisor
            ChatClient chatClient = ChatClient.builder(chatModel)
                    .defaultAdvisors(validationAdvisor)
                    .build();
            
            // 构建包含月份信息的prompt，要求返回该城市的月平均气温
            String prompt = String.format(
                    "请告诉我%s在%d月份的平均气温是多少？请提供城市名称、月份、平均温度（摄氏度）和该月份的天气特点描述。", 
                    city, month);
            
            WeatherInfo result = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .entity(WeatherInfo.class);  // 直接映射到POJO，Advisor会自动验证和重试
            
            // 判空处理
            if (Objects.isNull(result)) {
                throw new RuntimeException("获取天气信息失败，返回结果为null");
            }
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("获取天气信息成功，耗时: {}ms，城市: {}，月份: {}，平均温度: {}℃，描述: {}", 
                    duration, result.city(), result.month(), result.averageTemperature(), result.description());
            
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("获取天气信息失败，耗时: {}ms，城市: {}，月份: {}，错误信息: {}", 
                    duration, city, month, e.getMessage(), e);
            throw e;
        }
    }
}

