package com.qzx.xdupartner.controller;


import cn.hutool.core.bean.BeanUtil;
import com.qzx.xdupartner.constant.RedisConstant;
import com.qzx.xdupartner.entity.Message;
import com.qzx.xdupartner.entity.ReqMessage;
import com.qzx.xdupartner.entity.RspMessage;
import com.qzx.xdupartner.entity.vo.MessageVo;
import com.qzx.xdupartner.entity.vo.R;
import com.qzx.xdupartner.entity.vo.ResultCode;
import com.qzx.xdupartner.service.FriendService;
import com.qzx.xdupartner.service.MessageService;
import com.qzx.xdupartner.util.UserHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@Api
@RestController
@RequestMapping("/message")
public class MessageController {
    @Resource
    private FriendService friendService;
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

    @ApiOperation("")
    @GetMapping(value = "/connect", produces = "application/json;charset=utf-8")
    public R<List<MessageVo>> connect() {
        return new R<>(ResultCode.SUCCESS, messageService.connect());
    }

    @ApiOperation("")
    @PostMapping(value = "/sendMessage", produces = "application/json;charset=utf-8")
    public R<String> send(@Validated @RequestBody @NotNull ReqMessage reqMessage) {
        Message message = transferToMessage(reqMessage);
        Integer messageCount =
                messageService.lambdaQuery().eq(Message::getFromId, UserHolder.getUserId()).eq(Message::getToId,
                        reqMessage.getToId()).count();
        if (messageCount == 1 && friendService.judgeIfFriend(reqMessage.getToId())) {
            return new R<>(ResultCode.FAILED, "未成为好友前只能发送一条消息~");
        }
        messageService.sendMessage(message);
        return new R<>(ResultCode.SUCCESS, "发送成功");
    }


    @ApiOperation("")
    @PostMapping(value = "/readMessage", produces = "application/json;charset=utf-8")
    public R<String> read(@Validated @RequestParam @NotNull Long fromId,
                          @Validated @RequestParam @NotNull Long messageId) {
        stringRedisTemplate.delete(UserHolder.getUserId() + RedisConstant.OFFLINE_MESSAGE + fromId);
        boolean update = messageService.update().eq("from_id", fromId).eq("to_id", UserHolder.getUserId()).le("id",
                messageId
        ).set("is_received", 1).update();
        return new R<>(ResultCode.SUCCESS, "消息已读");
    }

    @ApiOperation("")
    @PostMapping(value = "/historyMessage", produces = "application/json;charset=utf-8")
    public R<List<RspMessage>> history(@Validated @RequestParam @NotNull Long fromId
            ,
                                       @Validated @RequestParam @NotNull Long messageId
    ) {
        return new R<>(ResultCode.SUCCESS, messageService.query10HistoryBellowEqualId(fromId, messageId));
    }
}

