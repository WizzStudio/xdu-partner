package com.qzx.xdupartner.entity.vo;

import com.qzx.xdupartner.entity.RspMessage;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("消息展示体")
public class MessageVo {
    @ApiModelProperty("来自用户")
    private UserVo userVo;
    @ApiModelProperty("消息列表")
    private List<RspMessage> messages;
    @ApiModelProperty("未读消息数")
    private Integer count;
    @ApiModelProperty("是否为好友")
    private Integer isFriend;
}
