package com.tericcabrel.authorization.configs;

import com.tericcabrel.authorization.converter.PlanckscaleJwtGrantedAuthoritiesConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;


@Configuration
@EnableWebSecurity
@Slf4j
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    @Value("${jwk.set.uri}")
    private String jwkSetUri;

    // @Autowired
    // AuthenticationFilter authFilter;

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Autowired
    public void globalUserDetails(AuthenticationManagerBuilder auth, UserDetailsService userDetailsService,
                                  PasswordEncoder encoder) throws Exception {
        auth.userDetailsService(userDetailsService)
                .passwordEncoder(encoder);
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, UserDetailsService userDetailsService) throws Exception {
        log.debug("enabling filter chain");
        String[] whitelistedUrls = {"/", "/token/**", "/auth/**", "/login", "/css/**", "/js/**"};

        http.authorizeHttpRequests(authorize ->
                        authorize.requestMatchers("/admin/**").hasRole("ADMIN")
                                .requestMatchers("/user/**").hasRole("USER")
                                .requestMatchers("/users/**").authenticated()
                                .requestMatchers("/hierarchy/**").authenticated()
                                .requestMatchers(whitelistedUrls).permitAll()
                                .anyRequest().permitAll()
//                                .requestMatchers("/auth/**").permitAll()
//                                .requestMatchers("/token/**").permitAll()
//                                .requestMatchers("favicon.ico").permitAll()
//                                .requestMatchers("/login", "/css/**", "/js/**").permitAll()
                )
                // .addFilterBefore(authFilter, AnonymousAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(
                                jwtDecoder()).jwtAuthenticationConverter(jwtAuthenticationConverter()))

                )
//                .csrf(csrf ->
//                        csrf.ignoringRequestMatchers(whitelistedUrls)
//                                .csrfTokenRepository(csrfTokenRepository()))
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                )
                .logout(logout -> logout
                        .permitAll())
                .sessionManagement(
                        session-> session.sessionCreationPolicy(
                                        SessionCreationPolicy.STATELESS))
                .userDetailsService(userDetailsService);
        return http.build();
    }

    JwtAuthenticationConverter jwtAuthenticationConverter() {
        PlanckscaleJwtGrantedAuthoritiesConverter grantedAuthoritiesConverter
                = new PlanckscaleJwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
        repository.setHeaderName("X-XSRF-TOKEN"); // Customize header name if needed
        return repository;
    }
}