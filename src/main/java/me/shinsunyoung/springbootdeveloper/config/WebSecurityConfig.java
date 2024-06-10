package me.shinsunyoung.springbootdeveloper.config;

import lombok.RequiredArgsConstructor;
import me.shinsunyoung.springbootdeveloper.config.OAuth.OAuth2AuthorizaionRequestBasedOnCookieRepository;
import me.shinsunyoung.springbootdeveloper.config.OAuth.OAuth2SsuccessHandler;
import me.shinsunyoung.springbootdeveloper.config.OAuth.OAuthUserCustomService;
import me.shinsunyoung.springbootdeveloper.config.jwt.TokenProvider;
import me.shinsunyoung.springbootdeveloper.repository.RefreshTokenRepository;
import me.shinsunyoung.springbootdeveloper.service.UserDetailService;
import me.shinsunyoung.springbootdeveloper.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import static org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console;

@Configuration
@RequiredArgsConstructor
public class WebSecurityConfig {
    // OAuth2 + JWT
    private final OAuthUserCustomService oAuthUserCustomService;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;

    // Disable Spring Security Feature
    @Bean
    public WebSecurityCustomizer configure() {
        return (web -> web.ignoring()
                .requestMatchers(toH2Console())
                .requestMatchers(
                        new AntPathRequestMatcher("/img/**"),
                        new AntPathRequestMatcher("/css/**"),
                        new AntPathRequestMatcher("/js/**")
                )
        );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Since using token-based authentication, disable the previously used form login and session
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // add custom filter to check head
                .addFilterBefore(tokenAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                // Reissue the token without requiring URL authentication, the rest of Apis require URL authentication
                .authorizeRequests(auth ->
                    auth.requestMatchers(
                            new AntPathRequestMatcher("/api/token")
                    )
                    .permitAll()
                    .requestMatchers(
                            new AntPathRequestMatcher("/api/**")
                    )
                        .authenticated()
                        .anyRequest()
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2.loginPage("/login")
                        // save status that has to with Authorization request
                    .authorizationEndpoint(authorizationEndpoint ->
                            authorizationEndpoint.authorizationRequestRepository(
                                    oAuth2AuthorizaionRequestBasedOnCookieRepository()
                            )
                    )
                    .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint.userService(oAuthUserCustomService))
                        // handler after success Authenticate
                    .successHandler(oAuth2SsuccessHandler())
                )
                // return 401 Error code if the url begin with "api"
                .exceptionHandling(exceptionHandling -> exceptionHandling.defaultAuthenticationEntryPointFor(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                        new AntPathRequestMatcher("/api/**")
                ))
                .build();
    }

    @Bean
    public OAuth2SsuccessHandler oAuth2SsuccessHandler() {
        return new OAuth2SsuccessHandler(
                tokenProvider,
                refreshTokenRepository,
                oAuth2AuthorizaionRequestBasedOnCookieRepository(),
                userService
        );
    }

    @Bean
    public TokenAuthenticationFilter tokenAuthenticationFilter() {
        return new TokenAuthenticationFilter((tokenProvider));
    }

    @Bean
    public OAuth2AuthorizaionRequestBasedOnCookieRepository oAuth2AuthorizaionRequestBasedOnCookieRepository() {
        return new OAuth2AuthorizaionRequestBasedOnCookieRepository();
    }


    // spring security
//    private final UserDetailService userService;
//    // Disable Spring Security Feature
//    @Bean
//    public WebSecurityCustomizer configure() {
//        return (web -> web.ignoring()
//                .requestMatchers(toH2Console())
//                .requestMatchers(new AntPathRequestMatcher("/static/**"))
//        );
//    }
//    // Web based security configuration for specific HTTP Requests
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        return http
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers(
//                                new AntPathRequestMatcher("/login"),
//                                new AntPathRequestMatcher("/signup"),
//                                new AntPathRequestMatcher("/user")
//                        )
//                        .permitAll()
//                        .anyRequest()
//                        .authenticated()
//                )
//                .formLogin(formlogin -> formlogin
//                        .loginPage("/login")
//                        .defaultSuccessUrl("/articles")
//                )
//                .logout(logout -> logout
//                        .logoutSuccessUrl("/login")
//                        .invalidateHttpSession(true)
//                ).csrf(AbstractHttpConfigurer::disable)
//                .build();
//    }
//    // Configuration for Authentication manager
//    @Bean
//    public AuthenticationManager authenticationManager(
//            HttpSecurity http,
//            BCryptPasswordEncoder bCryptPasswordEncoder,
//            UserDetailService userDetailService
//    ) throws Exception {
//        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
//        authProvider.setUserDetailsService(userService);
//        authProvider.setPasswordEncoder(bCryptPasswordEncoder);
//        return new ProviderManager(authProvider);
//    }
//    // Register Bean be used as a Password Encorder
//    @Bean
//    public BCryptPasswordEncoder bCryptPasswordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
}
