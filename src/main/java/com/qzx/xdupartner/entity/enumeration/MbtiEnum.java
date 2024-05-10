package com.qzx.xdupartner.entity.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MbtiEnum {
    未知(0, "未知"),
    ISTJ(1, "ISTJ-内倾感觉型思考者"),
    ISFJ(2, "ISFJ-内倾感觉型情感者"),
    INFJ(3, "INFJ-内倾直觉型情感者"),
    INTJ(4, "INTJ-内倾直觉型思考者"),
    ISTP(5, "ISTP-内倾感觉型思考型"),
    ISFP(6, "ISFP-内倾感觉型情感型"),
    INFP(7, "INFP-内倾直觉型情感型"),
    INTP(8, "INTP-内倾直觉型思考型"),
    ESTP(9, "ESTP-外倾感觉型思考型"),
    ESFP(10, "ESFP-外倾感觉型情感型"),
    ENFP(11, "ENFP-外倾直觉型情感型"),
    ENTP(12, "ENTP-外倾直觉型思考型"),
    ESTJ(13, "ESTJ-外倾感觉型思考者"),
    ESFJ(14, "ESFJ-外倾感觉型情感者"),
    ENFJ(15, "ENFJ-外倾直觉型情感者"),
    ENTJ(16, "ENTJ-外倾直觉型思考者");
    private final int id;
    private final String title;

    public static MbtiEnum match(Integer code) {
        for (MbtiEnum mbtiEnum : MbtiEnum.values()) {
            if (mbtiEnum.id == code) return mbtiEnum;
        }
        return 未知;
    }
}
