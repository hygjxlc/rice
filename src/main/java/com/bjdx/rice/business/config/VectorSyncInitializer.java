package com.bjdx.rice.business.config;

import com.bjdx.rice.business.service.vector.VectorSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 向量数据初始化器
 * 应用启动时自动检查并同步向量数据
 */
@Component
@Order(100)  // 在其他组件初始化完成后执行
public class VectorSyncInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(VectorSyncInitializer.class);

    @Autowired
    private VectorSearchProperties vectorProperties;

    @Autowired
    private VectorSyncService vectorSyncService;

    @Override
    public void run(ApplicationArguments args) {
        // 检查是否启用启动时自动同步
        if (!vectorProperties.getSync().isAutoSyncOnStartup()) {
            logger.info("========================================");
            logger.info("向量数据自动同步已关闭");
            logger.info("如需手动同步，请调用 API: POST /api/vector/admin/sync/all");
            logger.info("========================================");
            return;
        }
        
        if (!vectorProperties.getSearch().isEnabled()) {
            logger.info("向量检索功能未启用，跳过向量数据初始化");
            return;
        }

        logger.info("========================================");
        logger.info("开始检查向量数据同步状态...");
        logger.info("========================================");

        try {
            // 异步执行全量同步，不阻塞启动流程
            new Thread(() -> {
                try {
                    // 等待几秒确保数据库连接池已准备好
                    Thread.sleep(5000);
                    
                    logger.info("【向量同步】启动客户数据全量同步...");
                    vectorSyncService.rebuildAllCustomers();
                    
                    Thread.sleep(1000);
                    
                    logger.info("【向量同步】启动商品数据全量同步...");
                    vectorSyncService.rebuildAllProducts();
                    
                    logger.info("========================================");
                    logger.info("向量数据同步任务完成");
                    logger.info("========================================");
                } catch (Exception e) {
                    logger.error("向量数据同步过程中出错", e);
                }
            }, "vector-sync-initializer").start();
            
        } catch (Exception e) {
            logger.error("启动向量数据同步失败", e);
        }
    }
}
