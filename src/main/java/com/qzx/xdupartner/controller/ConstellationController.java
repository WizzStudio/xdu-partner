package com.qzx.xdupartner.controller;


import cn.hutool.core.bean.BeanUtil;
import com.qzx.xdupartner.entity.Constellation;
import com.qzx.xdupartner.entity.enumeration.ConstellationEnum;
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
@Api("星座控制层")
@RestController
@RequestMapping("/constellation")
public class ConstellationController {
    @ApiOperation("按id查询星座信息")
    @GetMapping(value = "/{id}")
    public R<Constellation> constellationInfo(@PathVariable int id) {
        ConstellationEnum match = ConstellationEnum.match(id);
        Constellation target = new Constellation();
        BeanUtil.copyProperties(match, target, true);
        return RUtil.success(target);
    }

    @ApiOperation("查询全部星座")
    @GetMapping(value = "/all")
    public R<List<Constellation>> constellations() {
        return RUtil.success(Arrays.stream(ConstellationEnum.values()).map((source) -> {
            Constellation target = new Constellation();
            BeanUtil.copyProperties(source, target, true);
            return target;
        }).collect(Collectors.toList()));
    }
}

