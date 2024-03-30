package com.qzx.xdupartner.controller;


import com.qzx.xdupartner.entity.Mbti;
import com.qzx.xdupartner.entity.vo.R;
import com.qzx.xdupartner.entity.vo.ResultCode;
import com.qzx.xdupartner.service.MbtiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@Api
@RestController
@RequestMapping("/mbti")
public class MbtiController {
    @Resource
    private MbtiService mbtiService;

    @ApiOperation("")
    @GetMapping(value = "/{id}")
    public R<Mbti> mbtiInfo(@PathVariable int id) {
        Mbti mbti = mbtiService.getById(id);
        if (mbti == null) {
            return new R<>(ResultCode.VALIDATE_ERROR, mbti);
        }
        return new R<>(ResultCode.SUCCESS, mbti);
    }

    @ApiOperation("")
    @GetMapping(value = "/all")
    public List<Mbti> mbtiAll() {
        return mbtiService.list();
    }
}

