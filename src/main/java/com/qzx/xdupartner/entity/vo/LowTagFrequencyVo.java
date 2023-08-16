package com.qzx.xdupartner.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LowTagFrequencyVo {
    String tag;
    Long count;
}
