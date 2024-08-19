package com.jxh.drivex.common.handler;

import com.jxh.drivex.common.execption.DrivexException;
import com.jxh.drivex.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<?> error(Exception e){
        log.error(e.getMessage(), e);
        return Result.fail();
    }

    @ExceptionHandler(DrivexException.class)
    public Result<?> error(DrivexException e) {
        log.error(e.getMessage(), e);
        return Result.build(null, e.getCode(), e.getMessage());
    }

}
