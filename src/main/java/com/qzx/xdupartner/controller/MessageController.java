package com.qzx.xdupartner.controller;


import cn.hutool.core.bean.BeanUtil;
import com.qzx.xdupartner.constant.RedisConstant;
import com.qzx.xdupartner.entity.Message;
import com.qzx.xdupartner.entity.ReqMessage;
import com.qzx.xdupartner.entity.RspMessage;
import com.qzx.xdupartner.entity.vo.MessageVo;
import com.qzx.xdupartner.service.MessageService;
import com.qzx.xdupartner.util.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author qzx
 * @since 2023-08-12
 */
@RestController
@RequestMapping("/message")
public class MessageController {

    @Resource
    private MessageService messageService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private Message transferToMessage(ReqMessage reqMessage) {
        Message message = BeanUtil.copyProperties(reqMessage, Message.class);
        message.setFromId(UserHolder.getUserId());
        message.setToId(reqMessage.getToId());
        message.setIsReceived(0);
        return message;
    }

    @GetMapping(value = "/connect", produces = "application/json;charset=utf-8")
    public List<MessageVo> connect() {
        return messageService.connect();
    }

    @PostMapping(value = "/sendMessage", produces = "application/json;charset=utf-8")
    public String send(@Validated @RequestBody @NotNull ReqMessage reqMessage) {
        Message message = transferToMessage(reqMessage);
        messageService.sendMessage(message);
        return "发送成功";
    }


    @PostMapping(value = "/readMessage", produces = "application/json;charset=utf-8")
    public String read(@Validated @RequestParam @NotNull Long fromId,
                       @Validated @RequestParam @NotNull Long messageId) {
        stringRedisTemplate.delete(UserHolder.getUserId() + RedisConstant.OFFLINE_MESSAGE + fromId);
        boolean update = messageService.update().eq("from_id", fromId).eq("to_id", UserHolder.getUserId()).le("id",
                messageId
        ).set("is_received", 1).update();
        return "消息已读";
    }

    @PostMapping(value = "/historyMessage", produces = "application/json;charset=utf-8")
    public List<RspMessage> history(@Validated @RequestParam @NotNull Long fromId
            ,
                                    @Validated @RequestParam @NotNull Long messageId
    ) {
        return messageService.query10HistoryBellowEqualId(fromId, messageId);
    }
}

