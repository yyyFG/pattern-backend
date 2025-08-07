package cn.y.usercenter.config;


import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.api.RedissonRxClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;

/**
 * Redisson 配置
 */
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonConfig {

    private String host;

    private String port;

    private String password;


    @Bean
    public RedissonClient redissonClient(){
        // 1. 创建配置
        Config config = new Config();
        // 这是多台的
//        config.useClusterServers()
//                // use "redis://" for Redis connection
//                // use "valkey://" for Valkey connection
//                // use "valkeys://" for Valkey SSL connection
//                // use "rediss://" for Redis SSL connection
//                .addNodeAddress("redis://127.0.0.1:7181");
        String redisAddress = String.format("redis://%s:%s", host,port);
        String redisPassword = "123456";
        config.useSingleServer().setAddress(redisAddress).setDatabase(3).setPassword(redisPassword);

        // 创建实例
        RedissonClient redisson = Redisson.create(config);

        return redisson;


        // or read config from file
//        config = Config.fromYAML(new File("config-file.yaml"));
    }
}
