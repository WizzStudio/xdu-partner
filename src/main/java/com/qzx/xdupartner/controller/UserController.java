package com.qzx.xdupartner.controller;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.qzx.xdupartner.constant.RedisConstant;
import com.qzx.xdupartner.entity.User;
import com.qzx.xdupartner.entity.dto.UserInfoDto;
import com.qzx.xdupartner.entity.vo.BlogVo;
import com.qzx.xdupartner.entity.vo.R;
import com.qzx.xdupartner.entity.vo.ResultCode;
import com.qzx.xdupartner.entity.vo.UserInfoVo;
import com.qzx.xdupartner.exception.APIException;
import com.qzx.xdupartner.service.BlogService;
import com.qzx.xdupartner.service.UserService;
import com.qzx.xdupartner.util.RUtil;
import com.qzx.xdupartner.util.UserHolder;
import com.qzx.xdupartner.util.UserUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    @PostMapping(value = "/change", produces = "application/json;charset=utf-8")
    public String ChangeUserInfoV2(@Validated @RequestBody UserInfoDto userInfoDto) {
        User user = UserUtil.transferToUser(userInfoDto);
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
    @GetMapping(value = "/me/info")
    public R<UserInfoVo> ofMeINfo() {
        User user = userService.getById(UserHolder.getUserId());
        return RUtil.success(UserUtil.convertToUserInfoVo(user));
    }


    @ApiOperation("")
    @GetMapping(value = "/logout")
    public R<String> logout() {
        UserHolder.removeUser();
        stringRedisTemplate.delete(RedisConstant.LOGIN_PREFIX + UserHolder.getUserSessionKey());
        return new R(ResultCode.SUCCESS, "登出成功");
    }

    @ApiOperation("")
    @GetMapping(value = "/query/my")
    public R<List<BlogVo>> queryMyselfPubV2(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return RUtil.success(blogService.getOnesBlogVo(UserHolder.getUserId(), current));
    }

    @ApiOperation("")
    @GetMapping(value = "/other/{userId}")
    public R<UserInfoVo> queryOtherV2(@PathVariable Long userId) {
        User byId = userService.getById(userId);
        if (byId == null) {
            throw new APIException("用户不存在！");
        }
        return RUtil.success(UserUtil.convertToUserInfoVo(byId));

    }

    @Deprecated
    @ApiOperation("")
    @GetMapping(value = "/queryMyselfPub")
    public R<List<BlogVo>> queryMyselfPub(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return RUtil.success(blogService.getOnesBlogVo(UserHolder.getUserId(), current));
    }


    @Deprecated
    @ApiOperation("")
    @GetMapping(value = "/otherUser/{userId}")
    public R<UserInfoDto> queryOther(@PathVariable Long userId) {
        User byId = userService.getById(userId);
        if (byId == null) {
            throw new APIException("用户不存在！");
        }
        return RUtil.success(UserUtil.getUserInfoVo(byId));

    }

    @Deprecated
    @PostMapping(value = "/login", produces = "application/json;charset=utf-8")
    public R<Map<String, Object>> login() {
        User user = userService.lambdaQuery().eq(User::getId, 35).one();
        if (ObjectUtil.isNull(user)) {
            return null;
        }
        user.setSessionKey("12345678");
        stringRedisTemplate.opsForValue().set(RedisConstant.LOGIN_PREFIX + "12345678",
                JSONUtil.toJsonStr(user),
                RedisConstant.LOGIN_VALID_TTL,
                TimeUnit.DAYS);
        UserHolder.saveUser(user);
        HashMap<String, Object> res = new HashMap<>(3);
        res.put("msg", "登录成功");
        res.put("token", "12345678");
        res.put("userId", 35);
        return RUtil.success(res);
    }

    @Deprecated
    @ApiOperation("")
    @PostMapping(value = "/changeUserInfo", produces = "application/json;charset=utf-8")
    public String ChangeUserInfo(@Validated @RequestBody UserInfoDto userInfoDto) {
        User user = UserUtil.transferToUser(userInfoDto);
        userService.updateById(user);
        stringRedisTemplate.opsForValue()
                .set(RedisConstant.LOGIN_PREFIX + UserHolder.getUserSessionKey(), JSONUtil.toJsonStr(user),
                        RedisConstant.LOGIN_VALID_TTL,
                        TimeUnit.HOURS);
        UserHolder.saveUser(userService.getById(UserHolder.getUserId()));
        stringRedisTemplate.delete(RedisConstant.USERVO_CACHE + UserHolder.getUserId());
        return "修改成功";
    }

    @Deprecated
    @ApiOperation("")
    @GetMapping(value = "/me")
    public R<UserInfoDto> ofMe() {
        User user = userService.getById(UserHolder.getUserId());
        return RUtil.success(UserUtil.getUserInfoVo(user));
    }
}

