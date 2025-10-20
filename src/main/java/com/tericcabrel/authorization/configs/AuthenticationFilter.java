package com.tericcabrel.authorization.configs;

import com.tericcabrel.authorization.utils.JwtTokenUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.tericcabrel.authorization.utils.Constants.*;

@Slf4j
@Component
public class AuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest req, HttpServletResponse res, FilterChain chain
    ) throws IOException, ServletException {
        String header = req.getHeader(HEADER_STRING);
        String username = null;
        String authToken = null;
        log.debug("Authorization {}", header);
        if (header != null && header.startsWith(TOKEN_PREFIX)) {
            authToken = header.replace(TOKEN_PREFIX,"");

            try {
                username = jwtTokenUtil.getUsernameFromToken(authToken);
                log.debug("fetched user from token {}", username);
            } catch (IllegalArgumentException e) {
                log.error(JWT_ILLEGAL_ARGUMENT_MESSAGE, e);
            } catch (ExpiredJwtException e) {
                log.warn(JWT_EXPIRED_MESSAGE, e);
            } catch(SignatureException e){
                log.error(JWT_SIGNATURE_MESSAGE);
            }
        } else {
            log.warn("couldn't find bearer string, will ignore the header");
        }
        log.debug("authentication context {}", SecurityContextHolder.getContext().getAuthentication());
        if (username != null
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            boolean isValidToken = jwtTokenUtil.validateToken(authToken, userDetails);
            log.debug("token is valid ? {}", isValidToken);
            if (Boolean.TRUE.equals(isValidToken)) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, "", userDetails.getAuthorities()
                );

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));

                log.info("authenticated the user {} with object {}", username, authentication);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } else {
            log.debug("user is not authenticated {}", authToken);
        }

        chain.doFilter(req, res);
    }
}
