package me.shinsunyoung.springbootdeveloper.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import me.shinsunyoung.springbootdeveloper.domain.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class TokenProvider {

    private final JwtProperties jwtProperties;

    public String generateToken(User user, Duration expiredAt) {
        Date now = new Date();
        return makeToken(
                new Date(now.getTime() + expiredAt.toMillis()),
                user
        );
    }
    // JWT Token creating method
    private String makeToken(Date expiry, User user) {
        Date now = new Date();

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .setSubject(user.getEmail())
                .claim("id", user.getId())
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getSecret_key())
                .compact();
    }
    // JWT Token validation method
    public boolean validToken(String token) {

        System.out.println("===== 리프레시 쿠키 잘 가져오는가?");
        System.out.println(token);
//        System.out.println(
//            Jwts.parser()
//                    .setSigningKey(jwtProperties.getSecret_key())
//                    .parseClaimsJws(token)
//                    .toString()
//        );
//        System.out.println("============================");


        try {
            Jwts.parser()
                    .setSigningKey(jwtProperties.getSecret_key())
                    .parseClaimsJws(token);

            return true;
        } catch (Exception e) {
            return false;
        }
    }
    // Token-based method to provide Authentication information
    public Authentication getAuthentication(String token) {
        Claims claims = getClaims(token);
        Set<SimpleGrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));

        return new UsernamePasswordAuthenticationToken(
                new org.springframework.security.core.userdetails.User(claims.getSubject(), "", authorities),
                token,
                authorities
        );
    }
    // Token-based method to provide User id
    public Long getUserId(String token) {
        Claims claims = getClaims(token);
        return claims.get("id", Long.class);
    }

    private Claims getClaims(String token) {
        // find claim and return
        return Jwts.parser()
                .setSigningKey(jwtProperties.getSecret_key())
                .parseClaimsJws(token)
                .getBody();
    }


}
