package org.txf.myblogsprinboot.advice;

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
}
