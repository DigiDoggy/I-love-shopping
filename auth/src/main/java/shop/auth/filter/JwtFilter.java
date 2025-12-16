package shop.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import shop.auth.service.JWTService;
import shop.auth.service.shopUserDetailsService;
import shop.auth.util.CookieUtils;

import java.io.IOException;


@RequiredArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final ApplicationContext applicationContext;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.startsWith("/oauth2/")
                || path.startsWith("/login/oauth2/")
                || path.equals("/login")
                || path.equals("/error")
                || path.equals("/api/auth/register")
                || path.equals("/api/auth/refresh")
                || path.startsWith("/actuator/")
                //only for testing reCAPTCHA
                || path.equals("/api/auth/recaptcha/verify")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            String email = null;
            try {
                email = jwtService.extractUserEmail(token);
            } catch (Exception e) {
                // we do not authenticate
                filterChain.doFilter(request, response);
                return;
            }

            if (email != null) {
                UserDetails userDetails = applicationContext.getBean(shopUserDetailsService.class).loadUserByUsername(email);
                if (jwtService.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        String cookieToken = CookieUtils.getCookieValue(request, "accessToken");
        return (cookieToken == null || cookieToken.isBlank()) ? null : cookieToken;
    }

}
