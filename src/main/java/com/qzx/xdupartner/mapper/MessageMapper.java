package com.qzx.xdupartner.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qzx.xdupartner.entity.Message;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author qzx
 * @since 2023-08-12
 */
public interface MessageMapper extends BaseMapper<Message> {
    @Select("select * from xdu_partner_dev.message where id <= #{messageId} and ((from_id = #{userId} and to_id = #{fromId}) or (from_id " +
            "= #{fromId} and to_id = #{userId})) order by id desc limit 10;")
    List<Message> query10HistoryBellowId(@Param("userId") Long userId, @Param("fromId") Long fromId, @Param("messageId") Long messageId);
}
