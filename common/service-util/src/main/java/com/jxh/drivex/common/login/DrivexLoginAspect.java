package com.jxh.drivex.common.login;

import com.jxh.drivex.common.constant.RedisConstant;
import com.jxh.drivex.common.execption.DrivexException;
import com.jxh.drivex.common.login.jwt.JwtUtil;
import com.jxh.drivex.common.result.ResultCodeEnum;
import com.jxh.drivex.common.util.AuthContextHolder;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Component
@Aspect
@Order(100)
public class DrivexLoginAspect {

    private final JwtUtil jwtUtil;

    public DrivexLoginAspect(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Before("execution(* com.jxh.drivex.*.controller.*.*(..)) && @annotation(DrivexLogin)")
    public void doBefore() {
        ServletRequestAttributes sra = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (sra == null) {
            throw new DrivexException(ResultCodeEnum.BAD_REQUEST);
        }
        HttpServletRequest request = sra.getRequest();
        String token = request.getHeader("token");
        if(Strings.isEmpty(token)) {
            throw new DrivexException(ResultCodeEnum.TOKEN_INVALID);
        }
        Claims claims = jwtUtil.parseToken(token);
        AuthContextHolder.setUserId(claims.get("userId", Long.class));
    }

}
