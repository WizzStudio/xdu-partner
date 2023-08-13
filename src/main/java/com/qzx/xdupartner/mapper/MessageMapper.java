package com.qzx.xdupartner.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qzx.xdupartner.entity.Message;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author qzx
 * @since 2023-08-12
 */
public interface MessageMapper extends BaseMapper<Message> {
    @Select("select * from xdu_partner.message where id <= #{messageId} and ((from_id = #{userId} and to_id = " +
            "#{fromId}) or (from_id " +
            "= #{fromId} and to_id = #{userId})) order by id desc limit 10;")
    List<Message> query10HistoryBellowId(@Param("userId") Long userId, @Param("fromId") Long fromId, @Param(
            "messageId") Long messageId);

    @Select("SELECT MAX(id) FROM xdu_partner.message")
    long selectMaxId();
}
