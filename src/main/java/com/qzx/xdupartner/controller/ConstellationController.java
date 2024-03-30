package com.qzx.xdupartner.controller;


import com.qzx.xdupartner.entity.Constellation;
import com.qzx.xdupartner.entity.vo.R;
import com.qzx.xdupartner.entity.vo.ResultCode;
import com.qzx.xdupartner.service.ConstellationService;
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
@RequestMapping("/constellation")
public class ConstellationController {
    @Resource
    private ConstellationService constellationService;

    @ApiOperation("")
    @GetMapping(value = "/{id}")
    public R<Constellation> constellationInfo(@PathVariable int id) {
        Constellation constellation = constellationService.getById(id);
        if (constellation == null) {
            return new R<>(ResultCode.VALIDATE_ERROR, null);
        }
        return new R<>(ResultCode.SUCCESS, constellation);
    }

    @ApiOperation("")
    @GetMapping(value = "/all")
    public R<List<Constellation>> constellations() {
        return new R<>(ResultCode.SUCCESS, constellationService.list());
    }
}

