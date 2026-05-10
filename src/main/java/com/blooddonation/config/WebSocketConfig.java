package com.blooddonation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable a simple message broker for in-memory messaging
        // Messages sent to "/topic" or "/queue" will be routed by the broker
        registry.enableSimpleBroker("/topic", "/queue"); 
        // Application destination prefixes for endpoints that clients can send messages to
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the WebSocket endpoint that the client will use to connect
        // This endpoint will be accessible at "/ws"
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*") // Allow connections from any origin (adjust for production)
                .withSockJS(); // Enable SockJS fallback options
    }
}
