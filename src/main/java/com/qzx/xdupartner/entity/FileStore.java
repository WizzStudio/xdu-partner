package com.qzx.xdupartner.entity;

import static com.qzx.xdupartner.constant.SystemConstant.URL_PREFIX;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * <p>
 *
 * </p>
 *
 * @author qzx
 * @since 2023-06-29
 */
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class FileStore implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 上传该文件的用户id
     */
    private Long userId;

    /**
     * 文件实际名称
     */
    private String realName;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 文件主名称
     */
    private String primaryName;

    /**
     * 文件扩展名
     */
    private String extension;

    /**
     * 存放路径
     */
    private String localPath;

    /**
     * 文件类型
     */
    private String type;

    /**
     * 文件大小
     */
    private Long size;

    /**
     * 上传时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    public FileStore(String realName, String fileName, String primaryName, String extension, String localPath,
                     String type, Long size, Long userId) {
        this.realName = realName;
        this.fileName = fileName;
        this.primaryName = primaryName;
        this.extension = extension;
        this.localPath = localPath;
        this.type = type;
        this.size = size;
        this.userId = userId;
    }

    public String getFileUri() {
        return URL_PREFIX + "upload/" + this.getType() + '/' + this.getFileName();
    }
}
