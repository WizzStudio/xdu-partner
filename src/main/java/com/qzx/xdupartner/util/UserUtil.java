package com.qzx.xdupartner.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.qzx.xdupartner.constant.RedisConstant;
import com.qzx.xdupartner.constant.SystemConstant;
import com.qzx.xdupartner.entity.User;
import com.qzx.xdupartner.entity.dto.UserInfoDto;
import com.qzx.xdupartner.entity.enumeration.ConstellationEnum;
import com.qzx.xdupartner.entity.enumeration.HighTag;
import com.qzx.xdupartner.entity.enumeration.MbtiEnum;
import com.qzx.xdupartner.entity.vo.UserInfoVo;
import com.qzx.xdupartner.exception.APIException;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class UserUtil {

    public static User transferToUser(UserInfoDto userInfoDto) {
        User user = BeanUtil.copyProperties(userInfoDto, User.class);
        user.setId(UserHolder.getUserId());
//        String icon = userInfoVo.getIcon();
//        if (StrUtil.isNotBlank(icon))
//            user.setIcon(AesUtil.decryptHex(icon));
        user.setIcon(userInfoDto.getIcon());
        List<String> picture = userInfoDto.getPicture();
        if (picture != null && !picture.isEmpty()) {
            if (picture.size() > 3) {
                throw new APIException("照片墙照片最多为3张");
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

    public static UserInfoDto getUserInfoVo(User user) {
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
        UserInfoDto userInfoDto = BeanUtil.toBean(user, UserInfoDto.class);
        userInfoDto.setPicture(collect);
        return userInfoDto;
    }

    public static User createUser(String phone, String openid) {
        User user = new User()
                .setPhone(phone)
                .setOpenId(openid)
                .setIcon(SystemConstant.DEFAULT_ICON_URL + RandomUtil.randomInt(SystemConstant.RANDOM_ICON_MIN,
                        SystemConstant.RANDOM_ICON_MAX) +
                        ".png")
                .setMyDescription("写几句话来描述一下自己吧~");
        StringRedisTemplate stringRedisTemplate = SpringUtil.getBean(StringRedisTemplate.class);
        user.setNickName(SystemConstant.DEFAULT_NICKNAME +
                stringRedisTemplate.opsForValue().increment(RedisConstant.DEFAULT_NICKNAME_INCREMENT));
        return user;
    }

    public static void setRedisData(User user, String token) {
        StringRedisTemplate stringRedisTemplate = SpringUtil.getBean(StringRedisTemplate.class);
        stringRedisTemplate.opsForValue().set(RedisConstant.LOGIN_PREFIX + token, JSONUtil.toJsonStr(user),
                RedisConstant.LOGIN_VALID_TTL, TimeUnit.DAYS);
    }

    public static UserInfoVo convertToUserInfoVo(User user) {
        UserInfoVo userInfoVo = new UserInfoVo();
        BeanUtil.copyProperties(user, userInfoVo, true);
        userInfoVo.setUserId(user.getId())
                .setConstellation(user.getConstellation())
                .setConstellationDesc(ConstellationEnum.match(user.getConstellation()).getTitle())
                .setMbti(user.getMbti())
                .setMbtiDesc(MbtiEnum.match(user.getMbti()).getTitle())
                .setPicture(StrUtil.split(user.getPicture(), SystemConstant.PICTURE_CONJUNCTION))
                .setHighTag(user.getHighTag())
                .setHighTagDesc(HighTag.match(user.getHighTag()).getDisplay())
                .setVerified(StrUtil.isNotBlank(user.getStuId()));
        return userInfoVo;
    }
}
