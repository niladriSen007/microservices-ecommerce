package com.niladri.productservice.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;

import org.springframework.data.redis.connection.RedisConnectionFactory;

import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.data.redis.serializer.*;

import java.time.Duration;

@Configuration
@EnableCaching
@Profile("!test") // Redis is not available in the test environment
public class RedisCacheConfig {

        // ✅ ObjectMapper (handles LocalDateTime + polymorphic types)
        @Bean
        public ObjectMapper redisObjectMapper() {
                ObjectMapper mapper = new ObjectMapper();
                mapper.registerModule(new JavaTimeModule());

                mapper.activateDefaultTyping(
                                LaissezFaireSubTypeValidator.instance,
                                ObjectMapper.DefaultTyping.NON_FINAL,
                                JsonTypeInfo.As.PROPERTY);

                return mapper;
        }

        // ✅ JSON serializer (shared everywhere)
        @Bean
        public RedisSerializer<Object> redisSerializer(ObjectMapper objectMapper) {
                return new GenericJackson2JsonRedisSerializer(objectMapper);
        }

        // ✅ Cache Manager (Spring Cache abstraction)
        @Bean
        public CacheManager cacheManager(RedisConnectionFactory connectionFactory,
                        RedisSerializer<Object> redisSerializer) {

                RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                                .prefixCacheNameWith("product-service::") // 🔥 IMPORTANT (namespace)
                                .entryTtl(Duration.ofMinutes(10)) // default TTL
                                .serializeKeysWith(
                                                RedisSerializationContext.SerializationPair
                                                                .fromSerializer(new StringRedisSerializer()))
                                .serializeValuesWith(
                                                RedisSerializationContext.SerializationPair
                                                                .fromSerializer(redisSerializer));

                return RedisCacheManager.builder(connectionFactory)
                                .cacheDefaults(config)
                                .transactionAware() // ✅ important if using @Transactional
                                .build();
        }

        // ✅ RedisTemplate (manual Redis operations)
        @Bean
        public RedisTemplate<String, Object> redisTemplate(
                        RedisConnectionFactory connectionFactory,
                        RedisSerializer<Object> redisSerializer) {

                RedisTemplate<String, Object> template = new RedisTemplate<>();

                template.setConnectionFactory(connectionFactory);

                // 🔑 Key serializers
                template.setKeySerializer(new StringRedisSerializer());
                template.setHashKeySerializer(new StringRedisSerializer());

                // 📦 Value serializers
                template.setValueSerializer(redisSerializer);
                template.setHashValueSerializer(redisSerializer);

                template.afterPropertiesSet();

                return template;
        }
}