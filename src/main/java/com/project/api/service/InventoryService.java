package com.project.api.service;

import com.project.api.entity.InventoryItem;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final ReactiveRedisOperations<String, InventoryItem> inventoryItemReactiveRedisOperations;

    public Flux<InventoryItem> get(String id) {
        return inventoryItemReactiveRedisOperations.keys(id)
                .flatMap(inventoryItemReactiveRedisOperations.opsForValue()::get);
    }

    public Flux<InventoryItem> update(InventoryItem inventoryItem) {
        return inventoryItemReactiveRedisOperations
                .opsForValue()
                .set(inventoryItem.getId(), inventoryItem)
                .thenMany(inventoryItemReactiveRedisOperations
                        .keys(inventoryItem.getId())
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
