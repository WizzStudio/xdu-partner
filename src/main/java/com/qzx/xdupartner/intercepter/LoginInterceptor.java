package com.qzx.xdupartner.intercepter;

import cn.hutool.json.JSONUtil;
import com.qzx.xdupartner.entity.vo.ResultVo;
import com.qzx.xdupartner.util.UserHolder;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Setter
@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (UserHolder.getUser() == null) {
            response.setStatus(401);
            response.setCharacterEncoding("utf-8");
            response.getWriter().write(JSONUtil.toJsonStr(new ResultVo(2000, "登录过期", "登录过期")));
            return false;
        }
        return true;
    }

    //业务完成后
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                                Exception ex) throws Exception {
        UserHolder.removeUser();
    }

}
