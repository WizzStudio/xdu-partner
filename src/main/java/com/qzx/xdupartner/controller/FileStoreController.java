package com.qzx.xdupartner.controller;


import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.utils.BinaryUtil;
import com.aliyun.oss.model.MatchMode;
import com.aliyun.oss.model.PolicyConditions;
import com.qzx.xdupartner.entity.vo.R;
import com.qzx.xdupartner.service.impl.ScheduleMission;
import com.qzx.xdupartner.util.RUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author qzx
 * @since 2023-06-29
 */
@Api("oss文件上传控制层")
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
    OSS ossClient = null;
    String endpoint = "oss-cn-hangzhou.aliyuncs.com";

    @PostConstruct
    public void getOssClient() {
        // 创建ossClient实例
        ossClient = new OSSClientBuilder().build(endpoint, accessId, accessKey);
    }

    @CrossOrigin
    @ApiOperation("阿里云文件上传签名")
    @GetMapping("/oss/policy")
    public R<Map<String, String>> policy() {

        String host = "https://" + bucket + "." + endpoint;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date = dateFormat.format(new Date());
        String dir = "upload/" + date + "/";

        try {
            long expireTime = 300;
            long expireEndTime = System.currentTimeMillis() + expireTime * 1000;
            Date expiration = new Date(expireEndTime);
            PolicyConditions policyConds = new PolicyConditions();
            policyConds.addConditionItem(PolicyConditions.COND_CONTENT_LENGTH_RANGE, 0, 1048576000);
            policyConds.addConditionItem(MatchMode.StartWith, PolicyConditions.COND_KEY, dir);

            String postPolicy = ossClient.generatePostPolicy(expiration, policyConds);
            byte[] binaryData = postPolicy.getBytes(StandardCharsets.UTF_8);
            String encodedPolicy = BinaryUtil.toBase64String(binaryData);
            String postSignature = ossClient.calculatePostSignature(postPolicy);

            Map<String, String> respMap = new LinkedHashMap<String, String>();
            respMap.put("accessid", accessId);
            respMap.put("policy", encodedPolicy);
            respMap.put("signature", postSignature);
            respMap.put("dir", dir);
            respMap.put("host", host);
            respMap.put("expire", String.valueOf(expireEndTime / 1000));
            return RUtil.success(respMap);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }
}

