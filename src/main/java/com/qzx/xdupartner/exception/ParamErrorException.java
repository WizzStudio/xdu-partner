package com.qzx.xdupartner.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ParamErrorException extends RuntimeException {

    public ParamErrorException(String message) {
        super(message);
    }

}