package com.nhomkha.assignment_java.config;

import com.nhomkha.assignment_java.repository.EmployeeRepository;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

@Configuration
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService users(EmployeeRepository repo) {
        return name -> {
            try {
                return repo.byUsername(name)
                        .map(EmployeePrincipal::new)
                        .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
            } catch (java.sql.SQLException e) {
                throw new InternalAuthenticationServiceException("Không thể kết nối dữ liệu đăng nhập.", e);
            }
        };
    }

    @Bean
    SecurityFilterChain security(HttpSecurity http, EmployeeRepository repo) throws Exception {
        return http.authorizeHttpRequests(
                        a ->
                                a.requestMatchers("/login", "/css/**", "/error")
                                        .permitAll()
                                        .requestMatchers(
                                                "/employees/**",
                                                "/suppliers/**",
                                                "/imports/**",
                                                "/categories/**",
                                                "/reports/**",
                                                "/products/new",
                                                "/products/*/edit")
                                        .hasRole("ADMIN")
                                        .requestMatchers(HttpMethod.POST, "/products/**")
                                        .hasRole("ADMIN")
                                        .anyRequest()
                                        .authenticated())
                .formLogin(l -> l.loginPage("/login").defaultSuccessUrl("/products", true).permitAll())
                .logout(
                        l ->
                                l.logoutSuccessUrl("/login?logout")
                                        .invalidateHttpSession(true)
                                        .deleteCookies("JSESSIONID"))
                .addFilterBefore(new AccountStateFilter(repo), AuthorizationFilter.class)
                .build();
    }
}
