package net.samitkumar.webfluxsecurityspa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.DelegatingServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authentication.HttpBasicServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.ServerAuthenticationEntryPointFailureHandler;
import org.springframework.security.web.server.authentication.WebFilterChainServerAuthenticationSuccessHandler;
import org.springframework.security.web.server.authorization.HttpStatusServerAccessDeniedHandler;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.pattern.PathPattern;
import reactor.core.publisher.Mono;

import java.util.Map;

@SpringBootApplication
@EnableWebFluxSecurity
public class WebfluxSecuritySpaApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebfluxSecuritySpaApplication.class, args);
	}

	@Bean
	public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
		http
				.authorizeExchange(exchanges -> exchanges
						.pathMatchers("/spa.html").permitAll()
						.anyExchange().authenticated()
				)
				//disable CORS for all the requests
				.cors(corsSpec -> corsSpec.configurationSource(request -> {
					var corsConfig = new CorsConfiguration();
					corsConfig.addAllowedOriginPattern("*");
					corsConfig.addAllowedMethod("*");
					corsConfig.addAllowedHeader("*");
					corsConfig.setAllowCredentials(true);
					return corsConfig;
				}))
				//For each request , there will be a csrf token in the cookie
				.csrf(csrf -> csrf
						.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse())
						.requireCsrfProtectionMatcher(new PathPatternParserServerWebExchangeMatcher("/api/**", HttpMethod.POST))
				)
				.formLogin(Customizer.withDefaults())
//				.formLogin((form) -> form
//						.authenticationSuccessHandler(new DelegatingServerAuthenticationSuccessHandler())
//						.authenticationFailureHandler(new ServerAuthenticationEntryPointFailureHandler(new HttpBasicServerAuthenticationEntryPoint()))
//				)
				//In case of any exception , it will return 401
				//.exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)))
				//Form login
				;
		return http.build();
	}

	@Bean
	RouterFunction<ServerResponse> routerFunction() {
		return RouterFunctions
				.route()
				.path("/api", builder -> builder
						.GET("", this::getHandler)
						.POST("", this::postHandler)
				)
				.build();
	}

	private Mono<ServerResponse> postHandler(ServerRequest request) {
		return request
				.bodyToMono(Map.class)
				.flatMap(map -> ServerResponse.ok().bodyValue(map));
	}

	private Mono<ServerResponse> getHandler(ServerRequest request) {
		return request
				.principal()
				.flatMap(ServerResponse.ok()::bodyValue);
	}
}

@Controller
class WebController {

	@GetMapping("/")
	public Mono<String> index() {
		return Mono.fromCallable(() -> "mvc");
	}
}