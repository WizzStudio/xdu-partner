package com.qzx.xdupartner.schedule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.qzx.xdupartner.constant.RedisConstant;
import com.qzx.xdupartner.entity.Blog;
import com.qzx.xdupartner.entity.ScheduleLog;
import com.qzx.xdupartner.entity.vo.LowTagFrequencyVo;
import com.qzx.xdupartner.mapper.BlogMapper;
import com.qzx.xdupartner.mapper.ScheduleLogMapper;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ScheduleMission {
    @Resource
    private ScheduleLogMapper scheduleLogMapper;
    @Resource
    private BlogMapper blogMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 定时更新问题浏览量到数据库中
     * 每天凌晨两点跑一次
     */
    @Scheduled(cron = "0 0 2 * * ?")
//    @Scheduled(cron = "0/5 0/1 * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void updateQuestionView() {
        log.info("开始执行浏览量入库");
        long start = System.currentTimeMillis();
        ScheduleLog scheduleLog = new ScheduleLog();
        scheduleLog.setName("定时更新浏览量");
        scheduleLog.setType(1);
        // 获取全部的key
        String pattern = RedisConstant.BLOG_READ_KEY + "*";
        Set<String> keys = stringRedisTemplate.keys(pattern);
        if (keys == null) {
            return;
        }
        try {
            for (String key : keys
            ) {
                Long viewTimes = stringRedisTemplate.opsForHyperLogLog().size(key);
                // 将key拆分
                String split = key.substring(key.lastIndexOf(':') + 1);
                // 根据问题id获取
                Blog blog = blogMapper.selectById(split);
                if (ObjectUtil.isEmpty(blog)) {
                    continue;
                }
                // 更改浏览量
                blog.setViewTimes(viewTimes.intValue() + blog.getViewTimes());
                int count = blogMapper.updateById(blog);
                if (count == 0) {
                    throw new RuntimeException("浏览量更新失败");
                }
                // 删除key
                stringRedisTemplate.delete(key);
            }
            long end = System.currentTimeMillis();
            log.info("浏览量入库结束，耗时：{}", end - start);
            scheduleLog.setStatus(1);
            scheduleLog.setRemark("任务执行成功");
            scheduleLog.setTime((int) (end - start));
        } catch (Exception e) {
            e.fillInStackTrace();
            long end = System.currentTimeMillis();
            scheduleLog.setStatus(2);
            scheduleLog.setRemark(e.getMessage());
            scheduleLog.setTime((int) (end - start));
        } finally {
            scheduleLogMapper.insert(scheduleLog);
        }
    }

    @Scheduled(cron = "0 0 4 * * ?")
//    @Scheduled(cron = "0/5 0/1 * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void update() {
        log.info("开始执行lowTag分词低频词清理");
        long start = System.currentTimeMillis();
        ScheduleLog scheduleLog = new ScheduleLog();
        scheduleLog.setName("定时清理低频词");
        scheduleLog.setType(2);
        try {
            for (int i = 1; i <= 4; i++) {
                String redisKey = RedisConstant.LOW_TAG_FREQUENCY + i;
                List<String> lowerThanOne = new ArrayList<>(50);
                List<String> keyList =
                        stringRedisTemplate.opsForHash().entries(redisKey).entrySet().stream().filter(entry -> Integer.parseInt(
                                (String) entry.getValue()) <= 1).map(entry -> String.valueOf(entry.getKey())).collect(Collectors.toList());
                stringRedisTemplate.opsForHash().delete(redisKey,keyList);
                log.info("移除了："+ keyList);
            }
            long end = System.currentTimeMillis();
            log.info("清理低频词，耗时：{}", end - start);
            scheduleLog.setStatus(1);
            scheduleLog.setRemark("任务执行成功");
            scheduleLog.setTime((int) (end - start));
        } catch (Exception e) {
            e.fillInStackTrace();
            long end = System.currentTimeMillis();
            scheduleLog.setStatus(2);
            scheduleLog.setRemark(e.getMessage());
            scheduleLog.setTime((int) (end - start));
        } finally {
            scheduleLogMapper.insert(scheduleLog);
        }
    }
}
