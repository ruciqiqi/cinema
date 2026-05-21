package com.cinema.config;

import com.cinema.entity.User;
import com.cinema.repository.UserRepository;
import com.cinema.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.util.List;

@Component
public class PasswordMigrationRunner implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(PasswordMigrationRunner.class);
    
    private final UserRepository userRepository;
    
    public PasswordMigrationRunner(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public void run(String... args) {
        List<User> users = userRepository.findAll();
        
        for (User user : users) {
            String currentPassword = user.getPassword();
            
            // 如果密码以 $2a$ 开头，说明已经是 BCrypt 格式，跳过
            if (currentPassword != null && currentPassword.startsWith("$2a$")) {
                continue;
            }
            
            // 如果是旧的 SHA-256 格式，需要迁移
            if (currentPassword != null && currentPassword.length() == 64) {
                logger.info("迁移用户 '{}' 的密码到 BCrypt 格式", user.getUsername());
                user.setPassword(PasswordUtil.encode("123456")); // 重置为默认密码
                userRepository.save(user);
                logger.info("用户 '{}' 密码已重置为默认密码 '123456'，请提醒用户登录后修改密码", user.getUsername());
            }
        }
        
        logger.info("密码迁移检查完成");
    }
}
