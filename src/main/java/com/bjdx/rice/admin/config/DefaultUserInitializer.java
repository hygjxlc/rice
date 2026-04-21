package com.bjdx.rice.admin.config;

// package com.bjdx.rice.admin.config;

import com.bjdx.rice.admin.entity.User;
import com.bjdx.rice.admin.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DefaultUserInitializer implements ApplicationRunner {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.default-admin.username:admin}")
    private String defaultUsername;

    @Value("${app.default-admin.password:Admin@123456}")
    private String defaultPassword;

    @Value("${app.default-admin.enabled:true}")
    private boolean enabled;

    @Override
    public void run(org.springframework.boot.ApplicationArguments args) {
        if (!enabled) {
            return;
        }

        // 检查用户是否已存在
        User existing = userMapper.findByUsername(defaultUsername);
        if (existing == null) {
            // 创建默认管理员
            User admin = new User();
            admin.setUsername(defaultUsername);
            admin.setPassword(passwordEncoder.encode(defaultPassword)); // 自动加 salt
            admin.setStatus(1); // 启用状态
            admin.setNickname("默认管理员");
            // 其他字段如 createTime 等按需设置

            userMapper.insert(admin); // 假设你有 insert 方法
            userMapper.insertUserRole(admin.getId(), 1L);

            System.out.println("✅ 默认管理员账户已创建: " + defaultUsername);
        } else {
            System.out.println("ℹ️ 默认管理员账户已存在: " + defaultUsername);
        }
    }
}