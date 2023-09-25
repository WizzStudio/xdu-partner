package com.qzx.xdupartner.service.impl;

import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qzx.xdupartner.constant.RedisConstant;
import com.qzx.xdupartner.constant.SystemConstant;
import com.qzx.xdupartner.entity.User;
import com.qzx.xdupartner.entity.vo.UserVo;
import com.qzx.xdupartner.exception.ApiException;
import com.qzx.xdupartner.mapper.UserMapper;
import com.qzx.xdupartner.service.UserService;

import cn.hutool.core.util.RandomUtil;
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
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public UserVo getUserVoById(Long userId) {
        String userVoJson = stringRedisTemplate.opsForValue().get(RedisConstant.USERVO_CACHE + userId);
        if (StrUtil.isNotBlank(userVoJson)) {
            stringRedisTemplate.expire(RedisConstant.USERVO_CACHE + userId, RedisConstant.CACHE_TTL,
                    TimeUnit.MINUTES);
            return JSONUtil.toBean(userVoJson, UserVo.class);
        }
        User byId = getById(userId);
        if (byId == null) {
            throw new ApiException("用户不存在！");
        }
        UserVo userVo = new UserVo();
        userVo.setId(byId.getId());
        userVo.setNickName(byId.getNickName());
        userVo.setIcon(byId.getIcon());
        stringRedisTemplate.opsForValue().set(RedisConstant.USERVO_CACHE + userId, JSONUtil.toJsonStr(userVo),
                RedisConstant.CACHE_TTL,
                TimeUnit.MINUTES);
        return userVo;
    }

    @Override
    public User insertNewUser(String stuId) {
        User user;
        user = new User();
        user.setMyDescription("写几句话来描述一下自己吧~");
        user.setStuId(stuId);
        //TODO 放入常量池，上传数据库图片，匿名的头像
        user.setIcon(SystemConstant.DEFAULT_ICON_URL + RandomUtil.randomInt(SystemConstant.RANDOM_ICON_MIN,
                SystemConstant.RANDOM_ICON_MAX) +
                ".jpg");
        user.setNickName(SystemConstant.DEFAULT_NICKNAME +
                stringRedisTemplate.opsForValue().increment(RedisConstant.DEFAULT_NICKNAME_INCREMENT));
        save(user);
        return user;
    }

}
