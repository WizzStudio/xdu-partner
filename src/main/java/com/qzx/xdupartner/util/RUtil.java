package com.qzx.xdupartner.util;

import com.qzx.xdupartner.entity.vo.R;
import com.qzx.xdupartner.entity.vo.ResultCode;

public class RUtil {
    public static <T> R<T> success(T t) {
        return new R<T>(ResultCode.SUCCESS).setData(t);
    }

    public static <T> R<T> error(ResultCode code, T t) {
        return new R<T>(code).setData(t);
    }

    public static <T> R<T> error(ResultCode code) {
        return new R<T>(code);
    }
}
