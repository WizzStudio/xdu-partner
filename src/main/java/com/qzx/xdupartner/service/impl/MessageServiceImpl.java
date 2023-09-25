package com.qzx.xdupartner.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qzx.xdupartner.constant.RedisConstant;
import com.qzx.xdupartner.entity.Message;
import com.qzx.xdupartner.entity.RspMessage;
import com.qzx.xdupartner.entity.vo.MessageVo;
import com.qzx.xdupartner.im.OnlineUserHolder;
import com.qzx.xdupartner.mapper.MessageMapper;
import com.qzx.xdupartner.service.FriendService;
import com.qzx.xdupartner.service.MessageService;
import com.qzx.xdupartner.service.UserService;
import com.qzx.xdupartner.util.UserHolder;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author qzx
 * @since 2023-08-12
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {
    //TODO 放入常量池
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);
    private static AtomicLong mid = null;
    @Resource
    private FriendService friendService;
    @Resource
    private UserService userService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private MessageMapper messageMapper;

    @PostConstruct
    public void init() {
        mid = new AtomicLong(messageMapper.selectMaxId());
    }

    private RspMessage transferToRspMessage(Message message) {
        RspMessage rspMessage = new RspMessage();
        rspMessage.setId(message.getId());
        rspMessage.setType(message.getType());
        rspMessage.setFromId(message.getFromId());
        rspMessage.setContent(message.getContent());
        return rspMessage;
    }

    @Override
    public void sendMessage(Message message) {
        message.setId(mid.incrementAndGet());
        message.setFromId(UserHolder.getUserId());
        message.setIsReceived(0);
        RspMessage rspMessage = transferToRspMessage(message);
        OnlineUserHolder.sendText(message.getToId(), rspMessage);
        String key = message.getToId() + RedisConstant.OFFLINE_MESSAGE + UserHolder.getUserId();
        stringRedisTemplate.opsForValue().increment(key);
        stringRedisTemplate.opsForValue()
                .set(message.getToId() + RedisConstant.HISTORY_MESSAGE + UserHolder.getUserId(),
                        JSONUtil.toJsonStr(rspMessage));
        stringRedisTemplate.opsForValue()
                .set(UserHolder.getUserId() + RedisConstant.HISTORY_MESSAGE + message.getToId(),
                        JSONUtil.toJsonStr(rspMessage));
        executor.submit(new Thread(() -> {
            save(message);
        }));
    }

    @Override
    public List<MessageVo> connect() {
        List<MessageVo> messageVoList = new ArrayList<>(20);
        //查看历史消息UserHolder.getUserId() + RedisConstant.HISTORY_MESSAGE + "*" 返回历史消息（1条的json）
        String historyKey = UserHolder.getUserId() + RedisConstant.HISTORY_MESSAGE + "*";
        Set<String> keys1 = stringRedisTemplate.keys(historyKey);
        if (keys1 != null) {
            for (String key : keys1) {
                String idStr = key.substring(key.lastIndexOf(':') + 1);
                Long otherId = Long.valueOf(idStr);
                String jsonStr1 = stringRedisTemplate.opsForValue().get(key);
                String jsonStr2 =
                        stringRedisTemplate.opsForValue().get(otherId + RedisConstant.HISTORY_MESSAGE + UserHolder.getUserId());
                if (!(StrUtil.isBlank(jsonStr1) && StrUtil.isBlank(jsonStr2))) {
                    MessageVo messageVo = new MessageVo();
                    Message message1 = JSONUtil.toBean(jsonStr1, Message.class);
                    Message message2 = JSONUtil.toBean(jsonStr2, Message.class);
                    if (StrUtil.isBlank(jsonStr1)) {
                        messageVo.setMessages(Collections.singletonList(transferToRspMessage(message2)));
                    } else if (StrUtil.isBlank(jsonStr2)) {
                        messageVo.setMessages(Collections.singletonList(transferToRspMessage(message1)));
                    } else if (message1.getId() > message2.getId()) {
                        messageVo.setMessages(Collections.singletonList(transferToRspMessage(message1)));
                    } else {
                        messageVo.setMessages(Collections.singletonList(transferToRspMessage(message2)));
                    }
                    messageVo.setUserVo(userService.getUserVoById(otherId));
                    messageVo.setIsFriend(friendService.judgeIfFriend(otherId) ? 1 : 0);
                    String offlineKey = UserHolder.getUserId() + RedisConstant.OFFLINE_MESSAGE + otherId;
                    String countStr = stringRedisTemplate.opsForValue().get(offlineKey);
                    if (StrUtil.isBlank(countStr)) {
                        messageVo.setCount(0);
                    } else {
                        messageVo.setCount(Integer.valueOf(countStr));
                    }
                    messageVoList.add(messageVo);
                }
            }
        }
        return messageVoList;
    }

    @Override
    public List<RspMessage> query10HistoryBellowEqualId(Long fromId, Long messageId) {
        List<Message> messageList = messageMapper.query10HistoryBellowId(UserHolder.getUserId(), fromId, messageId);
        return messageList.stream().map(this::transferToRspMessage).collect(Collectors.toList());
    }
}
