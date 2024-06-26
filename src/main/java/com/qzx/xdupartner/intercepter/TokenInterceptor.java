package com.qzx.xdupartner.intercepter;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.qzx.xdupartner.constant.RedisConstant;
import com.qzx.xdupartner.entity.User;
import com.qzx.xdupartner.entity.vo.ResultCode;
import com.qzx.xdupartner.util.JwtUtil;
import com.qzx.xdupartner.util.RUtil;
import com.qzx.xdupartner.util.UserHolder;
import io.jsonwebtoken.Claims;
import lombok.Setter;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Setter
@Component
public class TokenInterceptor implements HandlerInterceptor {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("token");
        //解析token->从redis拿出User->放入UserHolder
        if (StrUtil.isNotBlank(token)) {
            String redisKey = RedisConstant.LOGIN_PREFIX + token;
            User loginUser = JSONUtil.toBean(stringRedisTemplate.opsForValue().get(redisKey), User.class);
            if (Objects.isNull(loginUser)) {
                //todo 为了兼容老逻辑，最后都要切换为jwt
                String userid = null;
                try {
                    Claims claims = JwtUtil.parseJWT(token);
                    userid = claims.getSubject();
                } catch (Exception e) {
                    response.setStatus(401);
                    response.getWriter().write(JSONUtil.toJsonStr(RUtil.error(ResultCode.LOGIN_UNVALID)));
                    return false;
                }
                redisKey = RedisConstant.LOGIN_PREFIX + userid;
                loginUser = JSONUtil.toBean(stringRedisTemplate.opsForValue().get(redisKey), User.class);
                if (Objects.isNull(loginUser)) {
                    response.setStatus(401);
                    response.getWriter().write(JSONUtil.toJsonStr(RUtil.error(ResultCode.LOGIN_UNVALID)));
                    return false;
                }
                stringRedisTemplate.expire(redisKey, RedisConstant.LOGIN_VALID_TTL, TimeUnit.HOURS);
            }
            loginUser.setSessionKey(token);
            UserHolder.saveUser(loginUser);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
