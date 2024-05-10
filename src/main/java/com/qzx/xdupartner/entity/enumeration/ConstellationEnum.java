package com.qzx.xdupartner.entity.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <a href="https://blog.csdn.net/XY1790026787/article/details/107768555">...</a>
 */
@Getter
@AllArgsConstructor
public enum ConstellationEnum {
    未知(0, "未知", ""),
    白羊座(1, "白羊座", ""),
    金牛座(2, "金牛座 ", ""),
    双子座(3, "双子座", ""),
    巨蟹座(4, "巨蟹座 ", ""),
    狮子座(5, "狮子座 ", ""),
    处女座(6, "处女座 ", ""),
    天秤座(7, "天秤座 ", ""),
    天蝎座(8, "天蝎座", ""),
    射手座(9, "射手座", ""),
    摩羯座(10, "摩羯座 ", ""),
    水瓶座(11, "水瓶座 ", ""),
    双鱼座(12, "双鱼座", "");
    private final int id;
    private final String title;
    private final String description;

    public static ConstellationEnum match(Integer code) {
        for (ConstellationEnum constellation : ConstellationEnum.values()) {
            if (constellation.id == code) return constellation;
        }
        return 未知;
    }
}
