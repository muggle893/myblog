package org.txf.myblogsprinboot.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@SpringBootTest
public class JWTUtils {
    // 设置token的过期时间
    public static final long EXPIRE_TIME = 30 * 60 * 1000; // 设置成30分钟后过期
    public static final String SECRET_KEY = "xvPcU/zqoV6mbcbHNTPTyiwlM6ewYU2i9PvJVVsPqxw=";
    public static final Key KEY = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
    @Test
    public void test1() {
        // 测试jwt的使用方法
        // token分为三部分：
        // 1.alg
        // 2.payload
        // 3.signature签名，签名是用私钥对alg和body部分进行签名
        // 得到一个token字符串
        // payload部分是可以解析出来的
        // 所以重要的信息不能放到payload部分

        // 先生成秘钥
        System.out.println(Encoders.BASE64.encode(KEY.getEncoded()));
        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "zhangsan");
        payload.put("expire", System.currentTimeMillis() + EXPIRE_TIME);
        String token = Jwts.builder().addClaims(payload)
                .signWith(KEY)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRE_TIME))
                .compact();
        System.out.println(token);
    }

    @Test
    public void getKey() {
        // 先生成秘钥
        SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
        String encode = Encoders.BASE64.encode(secretKey.getEncoded());
        System.out.println(encode);
    }

    @Test
    public void verify() {
        String jwtToken = "eyJhbGciOiJIUzI1NiJ9" +
                ".eyJleHBpcmUiOjE3OTA0NzEwMjI5NDQsIm5hbWUiOiJ6aGFuZ3NhbiIsImV4cCI6MTc5MDQ3MTAyMn0" +
                ".NGTEXaEIxF5rk5Mw7uEYzngbi9TIOfkoaIH3RJcf478";
        JwtParser build = Jwts.parserBuilder().setSigningKey(KEY).build();
        Claims body = build.parseClaimsJws(jwtToken).getBody();
        System.out.println(body.get("name"));
    }
}
