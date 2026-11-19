package com.quinto.taller.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /** Después del login: el cliente va a su portal, el personal al panel del taller. */
    @Bean
    public AuthenticationSuccessHandler redireccionPorRol() {
        return (request, response, authentication) -> {
            boolean esCliente = authentication.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_CLIENTE".equals(a.getAuthority()));
            response.sendRedirect(request.getContextPath() + (esCliente ? "/portal" : "/taller"));
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CSRF activado: el frontend envía el token en la cabecera X-CSRF-TOKEN
            .csrf(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/login", "/registro", "/error", "/css/**", "/js/**", "/img/**").permitAll()

                // Portal del cliente
                .requestMatchers("/portal/**", "/api/portal/**").hasRole("CLIENTE")

                // Solo administrador
                .requestMatchers("/api/usuarios/**", "/api/reportes/**", "/api/auditoria/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/clientes/*/acceso").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/repuestos/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/repuestos/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/repuestos/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole("ADMIN")

                // Personal del taller
                .requestMatchers("/api/**", "/taller/**").hasAnyRole("ADMIN", "MECANICO")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(redireccionPorRol())
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            )
            // La API responde 401 en vez de redirigir al HTML del login cuando la sesión expira
            .exceptionHandling(ex -> ex.defaultAuthenticationEntryPointFor(
                    new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                    new AntPathRequestMatcher("/api/**")));

        return http.build();
    }
}
