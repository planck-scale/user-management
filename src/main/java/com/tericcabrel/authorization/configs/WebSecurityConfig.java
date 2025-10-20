package com.tericcabrel.authorization.configs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;


@Configuration
@EnableWebSecurity
@Slf4j
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    @Autowired
    AuthenticationFilter authFilter;

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
    public SecurityFilterChain filterChain(HttpSecurity http, UserDetailsService userDetailsService) throws Exception {
        log.debug("enabling filter chain");
        String[] whitelistedUrls = {"/", "/token/**", "/auth/**", "/login", "/css/**", "/js/**"};

        http.authorizeHttpRequests(authorize ->
                        authorize.requestMatchers("/admin/**").hasRole("ADMIN")
                                .requestMatchers("/user/**").hasRole("USER")
                                .requestMatchers("/users/**").authenticated()
                                .requestMatchers(whitelistedUrls).permitAll()
                                .anyRequest().permitAll()
//                                .requestMatchers("/auth/**").permitAll()
//                                .requestMatchers("/token/**").permitAll()
//                                .requestMatchers("favicon.ico").permitAll()
//                                .requestMatchers("/login", "/css/**", "/js/**").permitAll()
                )
                .addFilterBefore(authFilter, AnonymousAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable)
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

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
        repository.setHeaderName("X-XSRF-TOKEN"); // Customize header name if needed
        return repository;
    }

}