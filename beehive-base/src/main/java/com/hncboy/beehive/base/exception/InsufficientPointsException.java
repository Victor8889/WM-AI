package com.hncboy.beehive.base.exception;

/**
 * @author ll
 * @date 2023-3-23
 * 鉴权异常
 */
public class InsufficientPointsException extends RuntimeException {

    //@Getter
    //private final int resultCode;

    public InsufficientPointsException(String message) {
        super(message);
        //this.resultCode = HttpStatus.FORBIDDEN;
    }
}
