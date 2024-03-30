package com.qzx.xdupartner.controller;


import com.qzx.xdupartner.entity.Mbti;
import com.qzx.xdupartner.entity.vo.ResultCode;
import com.qzx.xdupartner.entity.vo.ResultVo;
import com.qzx.xdupartner.exception.ApiException;
import com.qzx.xdupartner.service.MbtiService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author qzx
 * @since 2023-08-12
 */
@RestController
@RequestMapping("/mbti")
public class MbtiController {
    @Resource
    private MbtiService mbtiService;

    @GetMapping(value = "/{id}")
    public ResultVo<Mbti> mbtiInfo(@PathVariable int id) {
        Mbti mbti = mbtiService.getById(id);
        if (mbti == null) {
            return new ResultVo<>(ResultCode.VALIDATE_ERROR, mbti);
        }
        return new ResultVo<>(ResultCode.SUCCESS, mbti);
    }

    @GetMapping(value = "/all")
    public List<Mbti> mbtiAll() {
        return mbtiService.list();
    }
}

