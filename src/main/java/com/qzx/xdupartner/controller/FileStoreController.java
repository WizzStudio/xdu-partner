package com.qzx.xdupartner.controller;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.qzx.xdupartner.constant.RedisConstant;
import com.qzx.xdupartner.schedule.ScheduleMission;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author qzx
 * @since 2023-06-29
 */
@RestController
@Slf4j
@RequestMapping("/api/file")
public class FileStoreController {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ScheduleMission scheduleMission;

    @Value("${accessId}")
    private String accessId;
    @Value("${accessKey}")
    private String accessKey;
    @Value("${bucket}")
    private String bucket;


    @CrossOrigin
    @GetMapping("/oss/policy")
    public Map<String, String> policy() {
        String endpoint = "oss-cn-hangzhou.aliyuncs.com";
        String host = "https://" + bucket + "." + endpoint;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = dateFormat.format(new Date());
        String dir = "upload/" + date + "/";
        // 创建ossClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessId, accessKey);
        try {
            long expireTime = 30;
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);

            String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes("utf-8");
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = ossClient.calculatePostSignature(postPolicy);

            Map<String, String> respMap = new LinkedHashMap<String, String>();
            respMap.put("accessid", accessId);
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", dir);
            respMap.put("host", host);
            respMap.put("expire", String.valueOf(expireEndTime / 1000));
            return respMap;
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @PostMapping(value = "/insertDict", produces = "application/json;charset=utf-8")
    public void insertDict(@Validated @NotBlank(message = "词语不能为空") @RequestParam String keyword) {
        String dict = stringRedisTemplate.opsForValue().get(RedisConstant.DICT_KEY);
        stringRedisTemplate.opsForValue().set(RedisConstant.DICT_KEY, dict + keyword + " 1 n\n");
        scheduleMission.update();
    }

}

