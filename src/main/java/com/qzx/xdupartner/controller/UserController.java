package com.qzx.xdupartner.controller;


import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.qzx.xdupartner.constant.RedisConstant;
import com.qzx.xdupartner.constant.SystemConstant;
import com.qzx.xdupartner.entity.User;
import com.qzx.xdupartner.entity.vo.BlogVo;
import com.qzx.xdupartner.entity.vo.UserInfoVo;
import com.qzx.xdupartner.exception.ApiException;
import com.qzx.xdupartner.service.BlogService;
import com.qzx.xdupartner.service.UserService;
import com.qzx.xdupartner.util.JwtUtil;
import com.qzx.xdupartner.util.UserHolder;
import com.qzx.xdupartner.util.XduAuthUtil;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author qzx
 * @since 2023-08-12
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    @Resource
    private BlogService blogService;
    @Resource
    private XduAuthUtil xduAuthUtil;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    private User transferToUser(UserInfoVo userInfoVo) {
        User user = BeanUtil.copyProperties(userInfoVo, User.class);
        user.setId(UserHolder.getUserId());
//        String icon = userInfoVo.getIcon();
//        if (StrUtil.isNotBlank(icon))
//            user.setIcon(AesUtil.decryptHex(icon));
        user.setIcon(userInfoVo.getIcon());
        List<String> picture = userInfoVo.getPicture();
        if (picture != null && !picture.isEmpty()) {
            if (picture.size() > 3) {
                throw new ApiException("照片墙照片最多为3张");
            }
//            List<String> collect = null;
//            try {
//                collect = picture.stream().map(AesUtil::decryptHex).collect(Collectors.toList());
//            } catch (Exception e) {
//                throw new ApiException("照片墙字串有误");
//            }
            String picStr = StrUtil.join(SystemConstant.PICTURE_CONJUNCTION, picture);
            if (StrUtil.isBlank(picStr)) {
                picStr = null;
            }
            user.setPicture(picStr);
        } else {
            user.setPicture(null);
        }
        return user;
    }

    private UserInfoVo getUserInfoVo(User user) {
//        FileStore icon = fileStoreService.getById(user.getIcon());
//        if (icon == null) {
//            user.setIcon(fileStoreService.getById(1L).getFileUri());
//        } else {
//            user.setIcon(icon.getFileUri());
//        }
        if (user.getIcon() == null || StrUtil.isBlank(user.getIcon())) {
            user.setIcon(
                    SystemConstant.DEFAULT_ICON_URL + RandomUtil.randomInt(SystemConstant.RANDOM_ICON_MIN,
                            SystemConstant.RANDOM_ICON_MAX) +
                            ".png");
        }
        String picture = user.getPicture();
        List<String> collect = null;
        if (StrUtil.isNotBlank(picture)) {
            collect = Arrays.asList(picture.split(SystemConstant.PICTURE_CONJUNCTION));
        }
        UserInfoVo userInfoVo = BeanUtil.toBean(user, UserInfoVo.class);
        userInfoVo.setPicture(collect);
        return userInfoVo;
    }

    @PostMapping(value = "/changeUserInfo", produces = "application/json;charset=utf-8")
    public String ChangeUserInfo(@Validated @RequestBody UserInfoVo userInfoVo) {
        User user = transferToUser(userInfoVo);
        userService.updateById(user);
        stringRedisTemplate.opsForValue()
                .set(RedisConstant.LOGIN_PREFIX + UserHolder.getUserId(), JSONUtil.toJsonStr(user),
                        RedisConstant.LOGIN_VALID_TTL,
                        TimeUnit.HOURS);
        UserHolder.saveUser(userService.getById(UserHolder.getUserId()));
        stringRedisTemplate.delete(RedisConstant.USERVO_CACHE + UserHolder.getUserId());
        return "修改成功";
    }

    @PostMapping(value = "/login", produces = "application/json;charset=utf-8")
    public Map<String, Object> login(@NotNull(message = "学号不能为空") @RequestParam("stuId") String stuId,
                                     @NotNull(message = "密码不能为空") @RequestParam("password") String password,
                                     @RequestParam("vcode") String vcode) {
        Integer login = null;
        long beginTime = System.currentTimeMillis();
        try {

            log.info("login: begin: stuId:{}, startTime:{}", stuId, beginTime);
            if (StrUtil.isNotBlank(vcode)) {
                login = xduAuthUtil.loginWithCaptcha(stuId, password, vcode);
            } else {
                login = xduAuthUtil.loginV2(stuId, password);
            }
            long endTime = System.currentTimeMillis();
            log.info("login: end: stuId:{}, endTime:{}, xduLoginCost:{}ms, loginResult:{}", stuId, endTime, endTime-beginTime, login);
        } catch (Exception e) {
            throw new RuntimeException("登录失败");
        }
        if (ObjectUtil.isNull(login) || login.equals(0)) {
            return new HashMap<String, Object>(1) {{
                put("msg", "登录失败");
            }};
        } else if (login.equals(2)) {
            return new HashMap<String, Object>(2) {{
                put("msg", "登录需要验证码，请到西电一站式网站手动登录成功后再来登录");
//                put("vcode", stringRedisTemplate.opsForHash().get(RedisConstant.NEED_CAPTCHA_USER + stuId, "img"));
            }};
        }
        User user = userService.lambdaQuery().eq(User::getStuId, stuId).one();
        if (ObjectUtil.isNull(user)) {
            user = userService.insertNewUser(stuId);
        }
        User finalUser = user;
        executor.submit(() -> stringRedisTemplate.opsForValue()
                .set(RedisConstant.LOGIN_PREFIX + finalUser.getId(), JSONUtil.toJsonStr(finalUser), RedisConstant.LOGIN_VALID_TTL,
                        TimeUnit.HOURS));
//        UserHolder.saveUser(user);
        HashMap<String, Object> res = new HashMap<>(3);
        res.put("msg", "登录成功");
        res.put("token", JwtUtil.createJWT(String.valueOf(user.getId())));
        res.put("userId", user.getId());
        long returnTime = System.currentTimeMillis();
        log.info("login: method return, returnTime:{}, cost:{}ms", returnTime, returnTime - beginTime);
        return res;
    }


    @GetMapping(value = "/me")
    public UserInfoVo ofMe() {
        User user = userService.getById(UserHolder.getUserId());
        return getUserInfoVo(user);
    }


    @GetMapping(value = "/logout")
    public String logout() {
        UserHolder.removeUser();
        stringRedisTemplate.delete(RedisConstant.LOGIN_PREFIX + UserHolder.getUserId());
        return "登出成功";
    }

    @GetMapping(value = "/queryMyselfPub")
    public List<BlogVo> queryMyselfPub(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return blogService.getOnesBlogVo(UserHolder.getUserId(), current);
    }

    @GetMapping(value = "/otherUser/{userId}")
    public UserInfoVo queryOther(@PathVariable Long userId) {
        User byId = userService.getById(userId);
        if (byId == null) {
            throw new ApiException("用户不存在！");
        }
        return getUserInfoVo(byId);
    }
}

