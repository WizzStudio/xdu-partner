package com.qzx.xdupartner.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@ApiModel("分词出现频率")
public class LowTagFrequencyVo {
    @ApiModelProperty("分词")
    String tag;
    @ApiModelProperty("出现次数")
    Long count;
}
