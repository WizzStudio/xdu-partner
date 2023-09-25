package com.qzx.xdupartner.entity.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HighTag {
    FUN(2, "娱乐"), LIFE(4, "生活"), STUDY(1, "学习"), LOVE(3, "恋爱");
    private final int code;
    private final String display;

    @Override
    public String toString() {
        return "HighTag{" +
                "code=" + code +
                ", display='" + display + '\'' +
                '}';
    }

    public static HighTag match(int code) {
        if (code == 1) {
            return STUDY;
        } else if (code == 2) {
            return FUN;
        } else if (code == 3) {
            return LOVE;
        } else if (code == 4) {
            return LIFE;
        }
        return STUDY;
    }
}
