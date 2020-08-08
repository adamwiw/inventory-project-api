package com.project.api.configuration;

import com.project.api.entity.InventoryItem;
import com.project.api.handler.InventoryHandler;
import com.project.api.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class ApiConfiguration {
    @Autowired
    private InventoryService inventoryService;

    @Bean
    public RouterFunction<ServerResponse> route() {
        InventoryHandler inventoryHandler = InventoryHandler
                .builder()
                .inventoryService(inventoryService)
                .build();

        return RouterFunctions
                .route(GET("/inventory/{id}").and(accept(APPLICATION_JSON)), inventoryHandler::inventoryGet)
                .andRoute(PUT("/inventory").and(accept(APPLICATION_JSON)), inventoryHandler::inventoryUpdate)
                .andRoute(DELETE("/inventory/{id}").and(accept(APPLICATION_JSON)), inventoryHandler::inventoryDelete);
    }

    @Bean
    ReactiveRedisOperations<String, InventoryItem> redisOperations(ReactiveRedisConnectionFactory factory) {
        Jackson2JsonRedisSerializer<InventoryItem> serializer = new Jackson2JsonRedisSerializer<>(InventoryItem.class);

        RedisSerializationContext.RedisSerializationContextBuilder<String, InventoryItem> builder =
                RedisSerializationContext.newSerializationContext(new StringRedisSerializer());

        RedisSerializationContext<String, InventoryItem> context = builder.value(serializer).build();

        return new ReactiveRedisTemplate<>(factory, context);
    }

}
