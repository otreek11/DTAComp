package com.dtacomp.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationEntryPointImpl authenticationEntryPoint;
    private final AccessDeniedHandlerImpl accessDeniedHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          AuthenticationEntryPointImpl authenticationEntryPoint,
                          AccessDeniedHandlerImpl accessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler)
            )
            .authorizeHttpRequests(auth -> auth
                // Endpoints Públicos
                .requestMatchers("/auth/**").permitAll()
                .requestMatchers("/hello").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
<<<<<<< Updated upstream
                
                // Conteúdos - Leitura pública / Apenas autenticados para escrita
=======

                // Conteúdos - Leitura pública / Escrita para membros, revisores e admins
>>>>>>> Stashed changes
                .requestMatchers(HttpMethod.GET, "/conteudos/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/conteudos/**").hasAnyRole("MEMBRO", "REVISOR", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/conteudos/**").hasAnyRole("MEMBRO", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/conteudos/**").hasAnyRole("MEMBRO", "ADMIN")

<<<<<<< Updated upstream
                // Revisões - Apenas revisor e admin
                .requestMatchers("/revisoes/**").hasAnyRole("REVISOR", "ADMIN")

                // Usuários e Instituições - Gestão administrativa
=======
                // Revisões - Apenas revisores e admins
                .requestMatchers("/revisoes/**").hasAnyRole("REVISOR", "ADMIN")

                // Gestão de Usuários e Instituições - Apenas admins
>>>>>>> Stashed changes
                .requestMatchers("/usuarios/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/instituicoes/**").authenticated()
                .requestMatchers("/instituicoes/**").hasRole("ADMIN")

<<<<<<< Updated upstream
                // Tags - Leitura autenticada, gestão ADMIN
=======
                // Tags - Leitura autenticada, gestão para admin
>>>>>>> Stashed changes
                .requestMatchers(HttpMethod.GET, "/tags/**").authenticated()
                .requestMatchers("/tags/**").hasRole("ADMIN")

                .anyRequest().authenticated()
            )
            .headers(headers ->
                    headers.frameOptions(frame -> frame.sameOrigin()))
            .addFilterBefore(jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
