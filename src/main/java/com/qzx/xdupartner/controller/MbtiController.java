package com.qzx.xdupartner.controller;


import cn.hutool.core.bean.BeanUtil;
import com.qzx.xdupartner.entity.Mbti;
import com.qzx.xdupartner.entity.enumeration.MbtiEnum;
import com.qzx.xdupartner.entity.vo.R;
import com.qzx.xdupartner.util.RUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author qzx
 * @since 2023-08-12
 */
@Api("mbti控制层")
@RestController
@RequestMapping("/mbti")
public class MbtiController {

    @ApiOperation("按id查询mbti")
    @GetMapping(value = "/{id}")
    public R<Mbti> mbtiInfo(@PathVariable int id) {
        MbtiEnum match = MbtiEnum.match(id);
        Mbti target = new Mbti();
        BeanUtil.copyProperties(match, target, true);
        return RUtil.success(target);
    }

    @ApiOperation("查询全部mbti")
    @GetMapping(value = "/all")
    public R<List<Mbti>> mbtiAll() {
        return RUtil.success(Arrays.stream(MbtiEnum.values()).map((source) -> {
            Mbti target = new Mbti();
            BeanUtil.copyProperties(source, target, true);
            return target;
        }).collect(Collectors.toList()));
    }
}

