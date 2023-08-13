package com.qzx.xdupartner.entity.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HighTag {
    FUN(0, "娱乐"), LIFE(1, "生活"), STUDY(2, "学习"), LOVE(3, "恋爱");
    private final int code;
    private final String display;

    @Override
    public String toString() {
        return "HighTag{" +
                "code=" + code +
                ", display='" + display + '\'' +
                '}';
    }
}
