package com.qzx.xdupartner.controller;


import java.util.List;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.qzx.xdupartner.entity.Constellation;
import com.qzx.xdupartner.exception.ApiException;
import com.qzx.xdupartner.service.ConstellationService;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author qzx
 * @since 2023-08-12
 */
@RestController
@RequestMapping("/constellation")
public class ConstellationController {
    @Resource
    private ConstellationService constellationService;

    @GetMapping(value = "/{id}")
    public Constellation constellationInfo(@PathVariable int id) {
        Constellation constellation = constellationService.getById(id);
        if (constellation == null) {
            throw new ApiException("mbti编号不存在");
        }
        return constellation;
    }

    @GetMapping(value = "/all")
    public List<Constellation> constellations() {
        return constellationService.list();
    }
}

