package com.qzx.xdupartner.entity.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@ApiModel("用户详情")
public class UserInfoVo {
    @ApiModelProperty("用户id")
    private Long userId;
    @ApiModelProperty("昵称")
    private String nickName;
    @ApiModelProperty("专业名")
    private String majorName;
    @ApiModelProperty("是否认证")
    private boolean isVerified;
    @ApiModelProperty("qq")
    private String qq;
    @ApiModelProperty("头像url")
    private String icon;
    @ApiModelProperty("照片墙图片url")
    private List<String> picture;
    @ApiModelProperty("")
    private String myDescription;
    @ApiModelProperty("需求倾向文案")
    private String highTag;
    @ApiModelProperty("人格类型文案")
    private String mbti;
    @ApiModelProperty("星座文案")
    private String constellation;

}
