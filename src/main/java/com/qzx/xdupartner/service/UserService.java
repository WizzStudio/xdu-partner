package com.qzx.xdupartner.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qzx.xdupartner.entity.User;
import com.qzx.xdupartner.entity.vo.UserVo;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author qzx
 * @since 2023-08-12
 */
public interface UserService extends IService<User> {


    UserVo getUserVoById(Long userId);

    User insertNewUser(String openId, String stuId);

    boolean checkUserIsVerified(Long userId);
}
