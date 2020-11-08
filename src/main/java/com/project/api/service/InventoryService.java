package com.project.api.service;

import com.project.api.entity.InventoryItem;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Setter
public class InventoryService {
    private final ReactiveRedisOperations<String, InventoryItem> inventoryItemReactiveRedisOperations;
    private String keyPrefix = "inventory:%s:%s";
    private Mono<Authentication> authenticationMono = ReactiveSecurityContextHolder
            .getContext()
            .map(SecurityContext::getAuthentication);

    public InventoryService(ReactiveRedisConnectionFactory factory) {
        Jackson2JsonRedisSerializer<InventoryItem> serializer = new Jackson2JsonRedisSerializer<>(InventoryItem.class);

        RedisSerializationContext.RedisSerializationContextBuilder<String, InventoryItem> builder =
                RedisSerializationContext.newSerializationContext(new StringRedisSerializer());

        RedisSerializationContext<String, InventoryItem> serializationContext = builder.value(serializer).build();

        inventoryItemReactiveRedisOperations = new ReactiveRedisTemplate<>(factory, serializationContext);
    }

    public Flux<InventoryItem> get(String id) {
        return ReactiveSecurityContextHolder.getContext()
                .switchIfEmpty(Mono.error(new IllegalStateException("ReactiveSecurityContext is empty")))
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getName)
                .flatMapMany(s -> inventoryItemReactiveRedisOperations.keys(String.format(keyPrefix, s, id))
                        .flatMap(inventoryItemReactiveRedisOperations.opsForValue()::get));
    }

    public Flux<InventoryItem> update(InventoryItem inventoryItem) {
        String key = StringUtils.isBlank(inventoryItem.getId()) ?
                UUID
                        .randomUUID()
                        .toString() :
                inventoryItem.getId();
        inventoryItem.setId(key);
        return ReactiveSecurityContextHolder.getContext()
                .switchIfEmpty(Mono.error(new IllegalStateException("ReactiveSecurityContext is empty")))
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getName)
                .flatMapMany(s -> inventoryItemReactiveRedisOperations
                        .opsForValue()
                        .set(String.format(keyPrefix, s, key), inventoryItem)
                        .thenMany(inventoryItemReactiveRedisOperations
                                .keys(String.format(keyPrefix, s, key))
                                .flatMap(inventoryItemReactiveRedisOperations.opsForValue()::get)));
    }

    public Flux<InventoryItem> delete(String id) {
        return ReactiveSecurityContextHolder.getContext()
                .switchIfEmpty(Mono.error(new IllegalStateException("ReactiveSecurityContext is empty")))
                .map(SecurityContext::getAuthentication)
                .map(Authentication::getName)
                .flatMapMany(s -> inventoryItemReactiveRedisOperations.keys(String.format(keyPrefix, s, id))
                        .flatMap(inventoryItemReactiveRedisOperations.opsForValue()::delete)
                        .thenMany(inventoryItemReactiveRedisOperations
                                .keys(String.format(keyPrefix, s, id))
                                .flatMap(inventoryItemReactiveRedisOperations.opsForValue()::get)));
    }
}
