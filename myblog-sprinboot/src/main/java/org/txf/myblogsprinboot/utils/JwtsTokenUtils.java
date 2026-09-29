package org.txf.myblogsprinboot.utils;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Slf4j
public class JwtsTokenUtils {
    // 设置token的过期时间
    public static final long EXPIRE_TIME = 24 * 60 * 60 * 1000; // 设置成一天后过期
    // 密钥
    public static final String SECRET_KEY = "xvPcU/zqoV6mbcbHNTPTyiwlM6ewYU2i9PvJVVsPqxw=";
    public static final Key KEY = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));


    public static String getJwtToken(Map claim) {
        String token = Jwts.builder()
                .signWith(KEY)
                .addClaims(claim)
                .setExpiration(new Date(EXPIRE_TIME + System.currentTimeMillis()))
                .compact();
        return token;
    }

    public static Claims parseJwtToken(String token) {
        JwtParser jwtParser = Jwts.parser().setSigningKey(KEY);
        Claims claims = null;
        try {
            claims = jwtParser.parseClaimsJws(token).getBody();

        }catch (ExpiredJwtException e){
            log.error(e.getMessage());
            throw e;
        } catch(Exception e) {
            log.error(e.getMessage());
            throw new JwtException("JwtToken验证失败, 无效的token.");
        }

        return claims;
    }
}
