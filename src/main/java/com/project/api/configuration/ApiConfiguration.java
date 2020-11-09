package com.project.api.configuration;

import com.project.api.handler.InventoryHandler;
import com.project.api.handler.UserHandler;
import com.project.api.security.JwtTokenProvider;
import com.project.api.service.InventoryService;
import com.project.api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class ApiConfiguration {
    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private UserService userService;

    @Autowired
    private ReactiveAuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Bean
    public RouterFunction<ServerResponse> route() {
        InventoryHandler inventoryHandler = InventoryHandler
                .builder()
                .inventoryService(inventoryService)
                .build();

        UserHandler userHandler = UserHandler
                .builder()
                .userService(userService)
                .passwordEncoder(passwordEncoder)
                .tokenProvider(jwtTokenProvider)
                .authenticationManager(authenticationManager)
                .build();

        return RouterFunctions
                .route(POST("/auth/sign-up").and(accept(APPLICATION_JSON)), userHandler::signUp)
                .andRoute(POST("/auth/sign-in").and(accept(APPLICATION_JSON)), userHandler::signIn)
                .andRoute(GET("/inventory/{id}").and(accept(APPLICATION_JSON)), inventoryHandler::inventoryGet)
                .andRoute(PUT("/inventory").and(accept(APPLICATION_JSON)), inventoryHandler::inventoryUpdate)
                .andRoute(DELETE("/inventory/{id}").and(accept(APPLICATION_JSON)), inventoryHandler::inventoryDelete);
    }
}
