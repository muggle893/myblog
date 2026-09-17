package org.txf.myblogsprinboot.exception;

/**
 * 这是参数异常类，当参数异常的时候可以抛出这个异常
 */

public class ParamErrorException extends RuntimeException {
    public ParamErrorException(String message) {
        super(message);
    }
}
