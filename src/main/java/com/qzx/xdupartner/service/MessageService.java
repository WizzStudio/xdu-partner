package com.qzx.xdupartner.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qzx.xdupartner.entity.Message;
import com.qzx.xdupartner.entity.RspMessage;
import com.qzx.xdupartner.entity.vo.MessageVo;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author qzx
 * @since 2023-08-12
 */
public interface MessageService extends IService<Message> {

    void sendMessage(Message message);

    List<MessageVo> connect();

    List<RspMessage> query10HistoryBellowEqualId(Long fromId, Long messageId);
}
