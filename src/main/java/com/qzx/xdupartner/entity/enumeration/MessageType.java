package com.qzx.xdupartner.entity.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MessageType {
    STRING(1), IMAGE(2), ADD_FRIEND(3);
    private final int code;
}
