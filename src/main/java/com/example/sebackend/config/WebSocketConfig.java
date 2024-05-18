package com.example.sebackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    // 配置template.convertAndSend("/air/requestServing" + new Response(200,"请求已完成",room));

    /**
     * 配置消息代理。
     * 该方法用于设置消息代理的前缀和应用的目的地前缀。
     *
     * @param config 用于配置消息代理的MessageBrokerRegistry对象。
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单消息代理并设置前缀为"/air"
        config.enableSimpleBroker("/air");
        // 设置应用目的地前缀为"/app"
        config.setApplicationDestinationPrefixes("/app");
    }

    /**
     * 注册STOMP端点到Spring WebSocket配置中。
     * 这个方法会为指定的URL路径注册一个STOMP端点，并允许来自任何源的请求通过SockJS技术进行访问。
     *
     * @param registry StompEndpointRegistry对象，用于注册STOMP端点。
     *                 它提供了添加端点并配置其属性的能力，比如允许的来源和启用SockJS。
     *                 SockJS是一种提供WebSocket兼容的JavaScript库和服务器协议。
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册端点"/ws"，允许所有来源的跨源请求，并启用SockJS作为备用传输机制。
        registry.addEndpoint("/ws").setAllowedOrigins("*").withSockJS();
    }
}
