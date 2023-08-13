package com.qzx.xdupartner.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qzx.xdupartner.entity.FileStore;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author qzx
 * @since 2023-08-12
 */
public interface FileStoreService extends IService<FileStore> {
    FileStore upload(String realName, MultipartFile multipartFile);
}
