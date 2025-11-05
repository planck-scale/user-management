package com.tericcabrel.authorization.converter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

@Slf4j
public class PlanckscaleJwtGrantedAuthoritiesConverter implements
        Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String DEFAULT_AUTHORITY_PREFIX = "SCOPE_";

    private static final Collection<String> WELL_KNOWN_AUTHORITIES_CLAIM_NAMES = Arrays.asList("scope", "scp",
            "roles", "permissions");

    private String authorityPrefix = DEFAULT_AUTHORITY_PREFIX;

    private String authoritiesClaimName;

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (String authority : getAuthorities(jwt)) {

            SimpleGrantedAuthority auth = new SimpleGrantedAuthority(this.authorityPrefix + authority);
            grantedAuthorities.add(auth);
        }
        log.debug("all authorities from jwt {}", grantedAuthorities);
        return grantedAuthorities;
    }

    /**
     * Sets the prefix to use for {@link GrantedAuthority authorities} mapped by this
     * converter. Defaults to
     * {@link JwtGrantedAuthoritiesConverter#DEFAULT_AUTHORITY_PREFIX}.
     * @param authorityPrefix The authority prefix
     * @since 5.2
     */
    public void setAuthorityPrefix(String authorityPrefix) {
        //Assert.notNull(authorityPrefix, "authorityPrefix cannot be null");
        this.authorityPrefix = authorityPrefix;
    }

    /**
     * Sets the name of token claim to use for mapping {@link GrantedAuthority
     * authorities} by this converter. Defaults to
     * {@link JwtGrantedAuthoritiesConverter#WELL_KNOWN_AUTHORITIES_CLAIM_NAMES}.
     * @param authoritiesClaimName The token claim name to map authorities
     * @since 5.2
     */
    public void setAuthoritiesClaimName(String authoritiesClaimName) {
        Assert.hasText(authoritiesClaimName, "authoritiesClaimName cannot be empty");
        this.authoritiesClaimName = authoritiesClaimName;
    }

    private String getAuthoritiesClaimName(Jwt jwt) {

        log.debug("authoritiesClaimName {}", authoritiesClaimName);
        if (this.authoritiesClaimName != null) {
            return this.authoritiesClaimName;
        }
        for (String claimName : WELL_KNOWN_AUTHORITIES_CLAIM_NAMES) {
            if (jwt.hasClaim(claimName)) {
                return claimName;
            }
        }
        return null;
    }

    private Collection<String> getAuthorities(Jwt jwt) {

        Collection<String> allAuthorities = new ArrayList<>();

        for(String claimName: WELL_KNOWN_AUTHORITIES_CLAIM_NAMES) {
            Object authorities = jwt.getClaim(claimName);
            log.debug("fetched object from jwt with attribute {} -> {}", claimName, authorities);
            if(authorities != null) {
                if (authorities instanceof String) {
                    if (StringUtils.hasText((String) authorities)) {
                        allAuthorities.addAll(Arrays.asList(((String) authorities).split(" ")));
                    }
                }
                if (authorities instanceof Collection) {
                    allAuthorities.addAll(castAuthoritiesToCollection(authorities));
                }
            }
        }
        return allAuthorities;
    }

    @SuppressWarnings("unchecked")
    private Collection<String> castAuthoritiesToCollection(Object authorities) {
        return (Collection<String>) authorities;
    }

}
