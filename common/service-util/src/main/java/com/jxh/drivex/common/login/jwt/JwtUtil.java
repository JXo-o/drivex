package com.jxh.drivex.common.login.jwt;

import com.jxh.drivex.common.execption.DrivexException;
import com.jxh.drivex.common.result.ResultCodeEnum;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * ClassName: JwtUtil
 * Package: com.jxh.drivex.common.config.jwt
 * Description:
 *
 * @author JX
 * @version 1.0
 * @date 2024/8/20 13:27
 */
@Component
@EnableConfigurationProperties(JwtProperties.class)
public class JwtUtil {


    private final SecretKey tokenSignKey;

    private final JwtProperties jwtProperties;

    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        tokenSignKey = Keys.hmacShaKeyFor(jwtProperties.getSignKey().getBytes());
    }

    public String createToken(Long userId) {
        return Jwts.builder()
                .subject("USER_INFO")
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration().toMillis()))
                .claim("userId", userId)
                .signWith(tokenSignKey)
                .compact();
    }

    public Claims parseToken(String token) {
        if (token == null) {
            throw new DrivexException(ResultCodeEnum.UNAUTHORIZED);
        }
        try {
            JwtParser jwtParser = Jwts.parser().verifyWith(tokenSignKey).build();
            return jwtParser.parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException e) {
            throw new DrivexException(ResultCodeEnum.TOKEN_EXPIRED);
        } catch (JwtException e) {
            throw new DrivexException(ResultCodeEnum.TOKEN_INVALID);
        }
    }

}
