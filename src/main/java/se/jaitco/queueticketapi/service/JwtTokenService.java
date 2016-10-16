package se.jaitco.queueticketapi.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import se.jaitco.queueticketapi.model.Roles;
import se.jaitco.queueticketapi.model.User;
import se.jaitco.queueticketapi.model.UserDetailsImpl;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtTokenService implements Serializable {

    private static final long serialVersionUID = -3301605591108950415L;

    static final String CLAIM_KEY_USERNAME = "username";

//    @Value("${filter.secret}")
    private String secret = "Secret";

//    @Value("${filter.expiration}")
    private Long expiration = 100000L;

    public UserDetailsImpl buildJWTUser(User user) {
        return UserDetailsImpl.builder().username(user.getUsername()).password(user.getPassword()).authorities(mapToGrantedAuthorities(user.getGrantedRoles())).build();
    }

    private static List<GrantedAuthority> mapToGrantedAuthorities(List<Roles> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toList());
    };

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .claim(CLAIM_KEY_USERNAME,userDetails.getUsername())
                .setExpiration(generateExpirationDate())
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public String getUsernameFromToken(String token) {
        final Claims claims = getClaimsFromToken(token);
        return (String)claims.get(CLAIM_KEY_USERNAME);
    }

    public Date getExpirationDateFromToken(String token) {
        final Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }

    private Claims getClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }

    protected Date generateExpirationDate() {
        return new Date(System.currentTimeMillis() + expiration * 1000);
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(getTodaysDate());
    }

    protected Date getTodaysDate() {
        return new Date();
    }
}