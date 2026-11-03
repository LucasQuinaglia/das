package br.com.sistemas.chamados.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/h2-console/**").permitAll()
                        .requestMatchers("/categorias/**", "/tecnicos/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/clientes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/clientes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/clientes/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/chamados/*/atribuir").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/chamados/*/status").hasAnyRole("ADMIN", "TECNICO")
                        .requestMatchers(HttpMethod.POST, "/chamados/*/comentarios").hasAnyRole("ADMIN", "TECNICO")
                        .requestMatchers(HttpMethod.PUT, "/chamados/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/chamados/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    UserDetailsService userDetailsService(
            PasswordEncoder encoder,
            @Value("${app.security.admin.username:admin}") String adminUsername,
            @Value("${app.security.admin.password:admin1234}") String adminPassword,
            @Value("${app.security.technician.username:tecnico}") String technicianUsername,
            @Value("${app.security.technician.password:tecnico1234}") String technicianPassword) {
        return new InMemoryUserDetailsManager(
                User.withUsername(adminUsername).password(encoder.encode(adminPassword)).roles("ADMIN").build(),
                User.withUsername(technicianUsername).password(encoder.encode(technicianPassword)).roles("TECNICO").build());
    }
}
