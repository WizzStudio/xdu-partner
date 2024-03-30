package com.qzx.xdupartner.controller;


import cn.hutool.json.JSONUtil;
import com.qzx.xdupartner.constant.RedisConstant;
import com.qzx.xdupartner.entity.User;
import com.qzx.xdupartner.entity.vo.BlogVo;
import com.qzx.xdupartner.entity.vo.R;
import com.qzx.xdupartner.entity.vo.ResultCode;
import com.qzx.xdupartner.entity.vo.UserInfoVo;
import com.qzx.xdupartner.exception.ApiException;
import com.qzx.xdupartner.service.BlogService;
import com.qzx.xdupartner.service.UserService;
import com.qzx.xdupartner.util.UserHolder;
import com.qzx.xdupartner.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author qzx
 * @since 2023-08-12
 */
@Api
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;
    @Resource
    private BlogService blogService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @ApiOperation("")
    @PostMapping(value = "/changeUserInfo", produces = "application/json;charset=utf-8")
    public String ChangeUserInfo(@Validated @RequestBody UserInfoVo userInfoVo) {
        User user = UserUtil.transferToUser(userInfoVo);
        userService.updateById(user);
        stringRedisTemplate.opsForValue()
                .set(RedisConstant.LOGIN_PREFIX + UserHolder.getUserSessionKey(), JSONUtil.toJsonStr(user),
                        RedisConstant.LOGIN_VALID_TTL,
                        TimeUnit.HOURS);
        UserHolder.saveUser(userService.getById(UserHolder.getUserId()));
        stringRedisTemplate.delete(RedisConstant.USERVO_CACHE + UserHolder.getUserId());
        return "修改成功";
    }

    @ApiOperation("")
    @GetMapping(value = "/me")
    public R<UserInfoVo> ofMe() {
        User user = userService.getById(UserHolder.getUserId());
        return new R<>(ResultCode.SUCCESS, UserUtil.getUserInfoVo(user));
    }

    @ApiOperation("")
    @GetMapping(value = "/logout")
    public R<String> logout() {
        UserHolder.removeUser();
        stringRedisTemplate.delete(RedisConstant.LOGIN_PREFIX + UserHolder.getUserSessionKey());
        return new R(ResultCode.SUCCESS, "登出成功");
    }

    @ApiOperation("")
    @GetMapping(value = "/queryMyselfPub")
    public R<List<BlogVo>> queryMyselfPub(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return new R<>(ResultCode.SUCCESS, blogService.getOnesBlogVo(UserHolder.getUserId(), current));
    }

    @ApiOperation("")
    @GetMapping(value = "/otherUser/{userId}")
    public R<UserInfoVo> queryOther(@PathVariable Long userId) {
        User byId = userService.getById(userId);
        if (byId == null) {
            throw new ApiException("用户不存在！");
        }
        return new R<>(ResultCode.SUCCESS, UserUtil.getUserInfoVo(byId));
    }
}

