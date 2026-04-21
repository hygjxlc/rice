package com.bjdx.rice.business.config;

import com.bjdx.rice.business.service.vector.EmbeddingService;
import com.bjdx.rice.business.service.vector.impl.NGramEmbeddingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 向量嵌入服务配置
 * 配置使用 N-Gram 算法作为主要嵌入服务
 */
@Configuration
public class EmbeddingConfig {

    /**
     * 配置 N-Gram 嵌入服务为主要服务
     * N-Gram 算法更适合短文本匹配，结果更接近 SQL 字符串匹配
     */
    @Bean
    @Primary
    public EmbeddingService embeddingService() {
        return new NGramEmbeddingService();
    }
}
