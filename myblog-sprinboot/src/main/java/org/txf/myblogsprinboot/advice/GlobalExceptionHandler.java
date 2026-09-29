package org.txf.myblogsprinboot.advice;

import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.txf.myblogsprinboot.exception.ParamErrorException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 处理业务参数校验失败。
     */
    @ExceptionHandler(ParamErrorException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleParamException(ParamErrorException exception) {
        return Result.paramError(exception.getMessage());
    }

    /**
     *  统一处理抛出运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result handleRuntimeException(RuntimeException exception) {
        return Result.fail(exception.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception exception) {
        return Result.fail(exception.getMessage());
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public Result handleExpiredJwtException(ExpiredJwtException exception) {
        return Result.nologin();
    }
}
