package com.qzx.xdupartner.intercepter;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.qzx.xdupartner.constant.RedisConstant;
import com.qzx.xdupartner.entity.User;
import com.qzx.xdupartner.util.JwtUtil;
import com.qzx.xdupartner.util.UserHolder;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.jsonwebtoken.Claims;
import lombok.Setter;

@Setter
@Component
public class TokenInterceptor implements HandlerInterceptor {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("token");
        //解析token->获取id->放入redis->放入UserHolder
        if (StrUtil.isNotBlank(token)) {
            String userid = null;
            try {
                Claims claims = JwtUtil.parseJWT(token);
                userid = claims.getSubject();
            } catch (Exception e) {
                response.setStatus(401);
                return false;
            }
            String redisKey = RedisConstant.LOGIN_PREFIX + userid;
            User loginUser = JSONUtil.toBean(stringRedisTemplate.opsForValue().get(redisKey), User.class);
            if (Objects.isNull(loginUser)) {
                response.setStatus(401);
                return false;
            }
            stringRedisTemplate.expire(redisKey, RedisConstant.LOGIN_VALID_TTL, TimeUnit.HOURS);
            UserHolder.saveUser(loginUser);
        }
        return true;
    }
}
