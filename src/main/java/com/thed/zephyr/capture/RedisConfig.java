package com.thed.zephyr.capture;

import com.thed.zephyr.capture.util.ApplicationConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Created by Masud on 4/4/19.
 */
@Configuration
@EnableRedisRepositories(basePackages = "com.thed.zephyr.capture.util")
public class RedisConfig {

    @Autowired
    private Environment env;

    @Value("${spring.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.redis.port:6379}")
    private Integer redisPort;

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public JedisConnectionFactory redisConnectionFactory() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        int maxTotal = env.getProperty(ApplicationConstants.JEDIS_POOL_CONFIG_MAX_TOTAL, Integer.class,8);
        int maxIdle = env.getProperty(ApplicationConstants.JEDIS_POOL_CONFIG_MAX_IDLE, Integer.class,8);
        poolConfig.setMaxTotal(maxTotal);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMaxIdle(maxIdle);

        JedisConnectionFactory redisConnectionFactory = new JedisConnectionFactory(poolConfig);
        redisConnectionFactory.setHostName(redisHost);
        redisConnectionFactory.setPort(redisPort);
        return redisConnectionFactory;
    }

//    @Bean
//    public JedisConnectionFactory jedisConnectionFactory() {
//        RedisProperties properties = redisProperties();
//        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
//        configuration.setHostName(properties.getHost());
//        configuration.setPort(properties.getPort());
//
//        return new JedisConnectionFactory(configuration);
//    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        final RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        template.setValueSerializer(new GenericToStringSerializer<>(Object.class));
        return template;
    }

    @Bean
    @Primary
    public RedisProperties redisProperties() {
        return new RedisProperties();
    }

    @Bean
    public JedisPool getUserJedisPool(){
        JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), redisHost, redisPort);
        return jedisPool;
    }
}

