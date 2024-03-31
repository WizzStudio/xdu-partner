package com.qzx.xdupartner.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MailCodeWrongException extends RuntimeException {

    public MailCodeWrongException(String message) {
        super(message);
    }

}
