package com.project.api.configuration;

import com.project.api.handler.InventoryHandler;
import com.project.api.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
}
