package com.qzx.xdupartner.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

import javax.validation.constraints.Size;

import com.qzx.xdupartner.constant.SystemConstant;
import com.qzx.xdupartner.util.JwtUtil;

import cn.hutool.json.JSONUtil;
import lombok.Data;

@Data
public class ReqMessage implements Serializable {
    //public class ReqMessage {
//
    private static final long serialVersionUID = 1L;

    private int command;

    private String token;

    private Long fromId;

    private Long toId;

    private int type;
    @Size(min = 1, max = 1000, message = "消息字数1~1000")
    private String content;

    private LocalDateTime createTime;

    public static void main(String[] args) {
        ReqMessage reqMessage = new ReqMessage();
        reqMessage.setCommand(SystemConstant.CommandType.PING);
        reqMessage.setToken(JwtUtil.createJWT("1"));
        reqMessage.setContent("ping");
        reqMessage.setFromId(1l);
        System.out.println(JSONUtil.toJsonStr(reqMessage));
    }
}
