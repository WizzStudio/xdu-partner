package com.qzx.xdupartner.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qzx.xdupartner.constant.RedisConstant;
import com.qzx.xdupartner.entity.Friend;
import com.qzx.xdupartner.mapper.FriendMapper;
import com.qzx.xdupartner.service.FriendService;
import com.qzx.xdupartner.util.UserHolder;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author qzx
 * @since 2023-08-12
 */
@Service
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend> implements FriendService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean judgeIfFriend(Long otherId) {
        String isFriendKey =
                stringRedisTemplate.opsForValue()
                        .getAndExpire("" + UserHolder.getUserId() + RedisConstant.IS_FRIEND + otherId,
                                RedisConstant.IS_FRIEND_TTL,
                                TimeUnit.MINUTES);
        if ("1".equals(isFriendKey)) {
            return true;
        }
        Integer count1 = query().eq("user_id", UserHolder.getUserId()).eq("friend_id", otherId).count();
        Integer count2 = query().eq("user_id", otherId).eq("friend_id", UserHolder.getUserId()).count();

        if (count1 == null || count2 == null || count1 == 0 || count2 == 0) {
            return false;
        }
        boolean isFriend = count1 + count2 >= 2;
        if (isFriend) {
            stringRedisTemplate.opsForValue().set("" + UserHolder.getUserId() + RedisConstant.IS_FRIEND + otherId, "1"
                    , RedisConstant.IS_FRIEND_TTL, TimeUnit.MINUTES);
        }
        return isFriend;

    }
}
