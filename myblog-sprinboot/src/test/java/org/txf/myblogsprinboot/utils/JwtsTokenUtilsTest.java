package org.txf.myblogsprinboot.utils;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class JwtsTokenUtilsTest {

    @Test
    void getJwtToken() {
        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("username", "zhangsan");
        claims.put("id", "234873");
        String jwtToken = JwtsTokenUtils.getJwtToken(claims);
        Claims claims1 = JwtsTokenUtils.parseJwtToken(jwtToken);
        System.out.println(claims1);
    }

    @Test
    void parseJwtToken() {

    }
}