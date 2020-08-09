package com.project.api.service;

import com.project.api.entity.InventoryItem;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Service
public class InventoryService {
    private final ReactiveRedisOperations<String, InventoryItem> inventoryItemReactiveRedisOperations;

    public InventoryService(ReactiveRedisConnectionFactory factory) {
        Jackson2JsonRedisSerializer<InventoryItem> serializer = new Jackson2JsonRedisSerializer<>(InventoryItem.class);

        RedisSerializationContext.RedisSerializationContextBuilder<String, InventoryItem> builder =
                RedisSerializationContext.newSerializationContext(new StringRedisSerializer());

        RedisSerializationContext<String, InventoryItem> context = builder.value(serializer).build();

        inventoryItemReactiveRedisOperations = new ReactiveRedisTemplate<>(factory, context);
    }

    public Flux<InventoryItem> get(String id) {
        return inventoryItemReactiveRedisOperations.keys(id)
                .flatMap(inventoryItemReactiveRedisOperations.opsForValue()::get);
    }

    public Flux<InventoryItem> update(InventoryItem inventoryItem) {
        String key = StringUtils.isBlank(inventoryItem.getId()) ?
                UUID.randomUUID().toString() :
                inventoryItem.getId();
        inventoryItem.setId(key);
        return inventoryItemReactiveRedisOperations
                .opsForValue()
                .set(key, inventoryItem)
                .thenMany(inventoryItemReactiveRedisOperations
                        .keys(key)
                        .flatMap(inventoryItemReactiveRedisOperations.opsForValue()::get));
    }

    public Flux<InventoryItem> delete(String id) {
        return inventoryItemReactiveRedisOperations.keys(id)
                .flatMap(inventoryItemReactiveRedisOperations.opsForValue()::delete)
                .thenMany(inventoryItemReactiveRedisOperations
                        .keys(id)
                        .flatMap(inventoryItemReactiveRedisOperations.opsForValue()::get));
    }
}
