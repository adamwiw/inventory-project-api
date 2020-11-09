package com.project.api.handler;

import com.project.api.entity.SignInResponse;
import com.project.api.entity.User;
import com.project.api.exception.UsernameAlreadyExistsException;
import com.project.api.security.JwtTokenProvider;
import com.project.api.service.UserService;
import lombok.Builder;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Collections;

@Builder
public class UserHandler {
    private final ReactiveAuthenticationManager authenticationManager;
    private UserService userService;
    private PasswordEncoder passwordEncoder;
    private JwtTokenProvider tokenProvider;

    public Mono<ServerResponse> signUp(ServerRequest request) {
        return request
                .bodyToMono(User.class)
                .flatMap(user -> userService
                        .get(user.getUsername())
                        .flatMap(user1 -> ServerResponse
                                .badRequest()
                                .body(Mono.defer(() ->
                                                Mono.error(new UsernameAlreadyExistsException("User Already Exists"))),
                                        String.class))
                        .switchIfEmpty(ServerResponse
                                .ok()
                                .body(userService
                                        .update(User
                                                .builder()
                                                .username(user.getUsername())
                                                .password(passwordEncoder.encode(user.getPassword()))
                                                .roles(Collections.singletonList("ROLE_USER"))
                                                .build())
                                        .next(), User.class)));
    }

    @CrossOrigin("https://baka-inventory-frontend.herokuapp.com")
    public Mono<ServerResponse> signIn(ServerRequest request) {
        return request
                .bodyToMono(User.class)
                .flatMap(user -> authenticationManager
                        .authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()))
                        .map(tokenProvider::createToken)
                )
                .flatMap(jwt -> {
                    return ServerResponse
                            .ok()
                            .header(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", jwt))
                            .body(Mono.just(new SignInResponse(jwt)), SignInResponse.class);
                });
    }
}
