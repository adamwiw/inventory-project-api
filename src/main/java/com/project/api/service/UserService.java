package com.project.api.service;

import com.project.api.entity.User;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class UserService {
    private final ReactiveRedisOperations<String, User> inventoryUserReactiveRedisOperations;

    public UserService(ReactiveRedisConnectionFactory factory) {
        Jackson2JsonRedisSerializer<User> serializer = new Jackson2JsonRedisSerializer<>(User.class);
        RedisSerializationContext.RedisSerializationContextBuilder<String, User> builder =
                RedisSerializationContext.newSerializationContext(new StringRedisSerializer());
        RedisSerializationContext<String, User> context = builder
                .value(serializer)
                .build();
        inventoryUserReactiveRedisOperations = new ReactiveRedisTemplate<>(factory, context);
    }

    public Mono<User> get(String username) {
        return inventoryUserReactiveRedisOperations
                .keys(String.format("user:%s", username))
                .flatMap(inventoryUserReactiveRedisOperations.opsForValue()::get)
                .next();
    }

    public Flux<User> update(User user) {
        String key = String.format("user:%s", user.getUsername());
        return inventoryUserReactiveRedisOperations
                .opsForValue()
                .set(key, user)
                .thenMany(inventoryUserReactiveRedisOperations
                        .keys(key)
                        .flatMap(inventoryUserReactiveRedisOperations.opsForValue()::get).map(user1 -> {
                            user1.setPassword("REDACTED");
                            return user1;
                        }));
    }
}
