package ch.hsr.servicestoolkit.editor.config;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import ch.hsr.servicestoolkit.editor.security.AuthoritiesConstants;

@Configuration
@EnableWebSocketMessageBroker
public class WebsocketConfiguration extends AbstractWebSocketMessageBrokerConfigurer {

	public static final String IP_ADDRESS = "IP_ADDRESS";

	@Override
	public void configureMessageBroker(final MessageBrokerRegistry config) {
		config.enableSimpleBroker("/topic");
	}

	@Override
	public void registerStompEndpoints(final StompEndpointRegistry registry) {
		registry.addEndpoint("/websocket/tracker").setHandshakeHandler(new DefaultHandshakeHandler() {
			@Override
			protected Principal determineUser(final ServerHttpRequest request, final WebSocketHandler wsHandler, final Map<String, Object> attributes) {
				Principal principal = request.getPrincipal();
				if (principal == null) {
					Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
					authorities.add(new SimpleGrantedAuthority(AuthoritiesConstants.ANONYMOUS));
					principal = new AnonymousAuthenticationToken("WebsocketConfiguration", "anonymous", authorities);
				}
				return principal;
			}
		}).withSockJS().setInterceptors(httpSessionHandshakeInterceptor());
	}

	@Bean
	public HandshakeInterceptor httpSessionHandshakeInterceptor() {
		return new HandshakeInterceptor() {

			@Override
			public boolean beforeHandshake(final ServerHttpRequest request, final ServerHttpResponse response, final WebSocketHandler wsHandler,
					final Map<String, Object> attributes) throws Exception {
				if (request instanceof ServletServerHttpRequest) {
					ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
					attributes.put(IP_ADDRESS, servletRequest.getRemoteAddress());
				}
				return true;
			}

			@Override
			public void afterHandshake(final ServerHttpRequest request, final ServerHttpResponse response, final WebSocketHandler wsHandler, final Exception exception) {

			}
		};
	}
}
