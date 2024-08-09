package com.wl2c.elswhereapigatewayserver.filter;

import com.wl2c.elswhereapigatewayserver.exception.AccessTokenRequiredException;
import com.wl2c.elswhereapigatewayserver.exception.LocalizedMessageException;
import com.wl2c.elswhereapigatewayserver.exception.LoggedOutAccessTokenException;
import com.wl2c.elswhereapigatewayserver.exception.NotGrantedException;
import com.wl2c.elswhereapigatewayserver.util.JwtUtil;
import com.wl2c.elswhereapigatewayserver.util.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.context.MessageSource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

@Component
@Slf4j
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private MessageSource messageSource;

    private static final String SIGN_UP_PATH = "/callback";
    private static final String LOGIN_PATH = "/login";
    private static final String ACTUATOR_PATH = "/actuator";
    private static final String SWAGGER_PATH = "/swagger-ui";
    private static final String SWAGGER_DOCS_PATH = "/v3/api-docs";
    private static final String HEALTH_CHECK = "/health_check";

    private static final List<String> TOKEN_AUTH_WHITELIST = List.of(SIGN_UP_PATH, LOGIN_PATH, ACTUATOR_PATH, SWAGGER_PATH, SWAGGER_DOCS_PATH, HEALTH_CHECK);
    private static final String ERROR_MESSAGE_FORMAT = "{\"errorCode\": \"%s\", \"errorMessage\": \"%s\"}";

    public AuthorizationHeaderFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            try {
                jwtUtil.getAccessTokenFromHeader(exchange.getRequest()).ifPresentOrElse(accessToken -> {
                        checkIfAccessTokenIsLogout(accessToken);

                        if (isTokenRefreshRequested(exchange.getRequest())) {
                            return;
                        }

                        jwtUtil.validateAccessToken(accessToken);
                        String userRole = jwtUtil.getUserRole(accessToken);

                        // 현재 요청 API 엔드포인트의 권한 검사
                        String requestPath = exchange.getRequest().getURI().getPath();
                        if (!hasPermission(requestPath, userRole)) {
                            throw new NotGrantedException();
                        }

                        exchange.getRequest().mutate()
                                .header("requestId", jwtUtil.getUserId(accessToken))
                                .header("requestRole", jwtUtil.getUserRole(accessToken)).build();

                    }, () -> {
                        if(tokenAuthenticationRequired(exchange.getRequest())) {
                            throw new AccessTokenRequiredException();
                        }
                    }
                );
            } catch (LocalizedMessageException e) {
                return onError(exchange.getResponse(), exchange.getLocaleContext().getLocale(), e);
            }
            return chain.filter(exchange);
        };
    }

    private boolean isTokenRefreshRequested(ServerHttpRequest request) {
        return request.getURI().getPath().contains("/reissue");
    }

    private boolean tokenAuthenticationRequired(ServerHttpRequest request) {
        return TOKEN_AUTH_WHITELIST.stream()
                .noneMatch(uri -> request.getURI().getPath().contains(uri));
    }

    private void checkIfAccessTokenIsLogout(String accessToken) {
        if (redisUtil.hasKey(accessToken)) {
            throw new LoggedOutAccessTokenException();
        }
    }

    private Mono<Void> onError(ServerHttpResponse response, Locale locale, LocalizedMessageException e) {
        response.setRawStatusCode(e.getStatusCode());
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String responseBody = String.format(ERROR_MESSAGE_FORMAT, e.getCode(), e.getMessages(messageSource, locale));
        DataBuffer buffer = response.bufferFactory().wrap(responseBody.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    private boolean hasPermission(String requestPath, String userRole) {
        // 요청 경로에 필요한 권한 체크
        // 예를 들어, 경로별로 필요한 최소 권한을 정의하고, 사용자 권한 목록과 비교하여 접근 여부를 결정
        if (requestPath.contains("/admin")) {
            return userRole.equals("ADMIN");
        }
        return true; // 권한이 필요하지 않은 경우 허용
    }

    public static class Config {
    }
}
