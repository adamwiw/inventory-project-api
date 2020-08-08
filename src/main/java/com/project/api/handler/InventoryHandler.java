package com.project.api.handler;

import com.project.api.entity.InventoryItem;
import com.project.api.service.InventoryService;
import lombok.Builder;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Builder
public class InventoryHandler {
    private InventoryService inventoryService;

    public Mono<ServerResponse> inventoryGet(ServerRequest serverRequest) {
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(inventoryService.get(serverRequest.pathVariable("id")), InventoryItem.class);
    }

    public Mono<ServerResponse> inventoryUpdate(ServerRequest serverRequest) {
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(serverRequest.
                        bodyToFlux(InventoryItem.class)
                        .flatMap(inventoryItem ->
                                inventoryService.update(inventoryItem)), InventoryItem.class);
    }

    public Mono<ServerResponse> inventoryDelete(ServerRequest serverRequest) {
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(inventoryService.delete(serverRequest.pathVariable("id")), InventoryItem.class);
    }
}