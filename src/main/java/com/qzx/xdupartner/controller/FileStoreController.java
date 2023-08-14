package com.qzx.xdupartner.controller;


import cn.hutool.core.io.FileUtil;
import com.qzx.xdupartner.entity.FileStore;
import com.qzx.xdupartner.service.FileStoreService;
import com.qzx.xdupartner.util.AesUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

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
    private FileStoreService fileStoreService;

    @Value("${file.local-path}")
    private String path;
    @Value("${file.dict-path}")
    private String dictPath;

    @PostConstruct
    public void initPath() {
        //windows
        if (!path.startsWith("/"))
            path = path.replace('/', '\\') + '\\';
//                .substring(1, path.length());
        log.info("upload path: " + path);
    }

    @PostMapping(value = "/upload", consumes = "multipart/*", headers = {"content-type=multipart/form-data", "content" +
            "-type=application/json"})
    public Map<String, String> uploadFile(@RequestParam("file") @NotNull(message = "图片不能为空") MultipartFile multipartFile) {
        FileStore upload = fileStoreService.upload(multipartFile.getOriginalFilename(), multipartFile);
//        return "upload/"+upload.getType()+'/'+upload.getFileName();
        HashMap<String, String> map = new HashMap<>();
        map.put("code", AesUtil.encryptHex("" + upload.getId()));
        map.put("uri", upload.getFileUri());
        log.info("文件上传完成");
        return map;
    }

    @PostMapping(value = "/insertDict", produces = "application/json;charset=utf-8")
    public void insertDict(@Validated @NotBlank(message = "词语不能为空") @RequestParam String keyword) {
        FileUtil.appendString(keyword + " 1 n\n", dictPath, StandardCharsets.UTF_8);
    }

}

