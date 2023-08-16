package com.qzx.xdupartner.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qzx.xdupartner.entity.Friend;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author qzx
 * @since 2023-08-12
 */
public interface FriendService extends IService<Friend> {

    boolean judgeIfFriend(Long otherId);
}
