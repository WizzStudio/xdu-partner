package com.qzx.xdupartner.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.qzx.xdupartner.constant.SystemConstant;
import com.qzx.xdupartner.entity.User;
import com.qzx.xdupartner.entity.vo.UserInfoVo;
import com.qzx.xdupartner.exception.ApiException;

import java.util.Arrays;
import java.util.List;

public class UserUtil {
    public static User transferToUser(UserInfoVo userInfoVo) {
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

    public static UserInfoVo getUserInfoVo(User user) {
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
}
