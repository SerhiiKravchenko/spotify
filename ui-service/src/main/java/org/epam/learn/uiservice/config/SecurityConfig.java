package org.epam.learn.uiservice.config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${auth.server.end-session-uri:${AUTH_SERVER_END_SESSION_URI:http://localhost:8080/connect/logout}}")
    private String endSessionUri;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**", "/css/**", "/js/**", "/webjars/**", "/favicon.ico").permitAll()
                .requestMatchers(HttpMethod.POST, "/storages/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .oauth2Login(login -> login
                .userInfoEndpoint(userInfo -> userInfo.userAuthoritiesMapper(userAuthoritiesMapper())))
            .logout(logout -> logout
                .logoutSuccessHandler(oidcLogoutSuccessHandler())
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("UISESSIONID"));
        return http.build();
    }

    private LogoutSuccessHandler oidcLogoutSuccessHandler() {
        return (request, response, authentication) -> {
            String postLogoutRedirectUri = ServletUriComponentsBuilder.fromContextPath(request)
                    .path("/").build().toUriString();
            UriComponentsBuilder logout = UriComponentsBuilder.fromUriString(endSessionUri)
                    .queryParam("post_logout_redirect_uri", postLogoutRedirectUri);
            if (authentication != null && authentication.getPrincipal() instanceof OidcUser oidcUser) {
                logout.queryParam("id_token_hint", oidcUser.getIdToken().getTokenValue());
            }
            response.sendRedirect(logout.encode().build().toUriString());
        };
    }

    private GrantedAuthoritiesMapper userAuthoritiesMapper() {
        return authorities -> {
            Set<GrantedAuthority> mapped = new HashSet<>(authorities);
            for (GrantedAuthority authority : authorities) {
                if (authority instanceof OidcUserAuthority oidcUserAuthority) {
                    List<String> roles = oidcUserAuthority.getIdToken().getClaimAsStringList("roles");
                    if ((roles == null || roles.isEmpty()) && oidcUserAuthority.getUserInfo() != null) {
                        roles = oidcUserAuthority.getUserInfo().getClaimAsStringList("roles");
                    }
                    if (roles != null) {
                        roles.forEach(role -> mapped.add(new SimpleGrantedAuthority("ROLE_" + role)));
                    }
                }
            }
            return mapped;
        };
    }
}
