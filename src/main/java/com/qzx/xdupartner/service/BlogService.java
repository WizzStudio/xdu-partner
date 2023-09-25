package com.qzx.xdupartner.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qzx.xdupartner.entity.Blog;
import com.qzx.xdupartner.entity.vo.BlogVo;
import com.qzx.xdupartner.entity.vo.LowTagFrequencyVo;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author qzx
 * @since 2023-08-12
 */
public interface BlogService extends IService<Blog> {
    void saveBlog(Blog blog, List<String> tagWords);

    boolean completeBlog(Long id);

    boolean likeBlog(Long id);

    boolean updateBlog(Blog blog);

    boolean deleteBlog(Long id);

    BlogVo getVoById(Long id);

    List<LowTagFrequencyVo> getTagWordCount(Integer typeId);

    List<BlogVo> search(String keyword, Integer current);

    List<BlogVo> searchByTypeIdContainsLowTag(Integer typeId, String keyword, Integer current);

    List<BlogVo> getOnesBlogVo(Long userId, Integer current);

    List<BlogVo> getHottest(Integer current);

    List<BlogVo> getNewest(Integer current);

    List<BlogVo> getLike(Integer current);


}
