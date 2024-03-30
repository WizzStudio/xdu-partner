package com.qzx.xdupartner.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import com.huaban.analysis.jieba.JiebaSegmenter;
import com.huaban.analysis.jieba.WordDictionary;
import com.qzx.xdupartner.constant.RedisConstant;
import com.qzx.xdupartner.constant.SystemConstant;
import com.qzx.xdupartner.entity.Blog;
import com.qzx.xdupartner.entity.ScheduleLog;
import com.qzx.xdupartner.mapper.BlogMapper;
import com.qzx.xdupartner.mapper.ScheduleLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.qzx.xdupartner.constant.RedisConstant.BLOG_READ_KEY;

@Slf4j
@Service
public class ScheduleMission {
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);
    @Resource
    private ScheduleLogMapper scheduleLogMapper;
    @Resource
    private BlogMapper blogMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Scheduled(cron = "0 0 4 * * ?")
//    @Scheduled(cron = "0/5 0/1 * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void flush() {
        log.info("开始执行lowTag分词低频词清理");
        long start = System.currentTimeMillis();
        ScheduleLog scheduleLog = new ScheduleLog();
        scheduleLog.setName("定时清理低频词");
        scheduleLog.setType(2);
        try {
            for (int i = 1; i <= 4; i++) {
                String redisKey = RedisConstant.LOW_TAG_FREQUENCY + i;
                Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(redisKey);
                if (entries.isEmpty()) {
                    continue;
                }
                List<String> keyList =
                        entries.entrySet().stream().filter(entry -> Integer.parseInt(
                                (String) entry.getValue()) <= 1).map(entry -> String.valueOf(entry.getKey())).collect(Collectors.toList());
                Object[] fields = keyList.toArray();
                stringRedisTemplate.opsForHash().delete(redisKey, fields);
                log.info("移除了：" + keyList);
            }
            long end = System.currentTimeMillis();
            log.info("清理低频词，耗时：{}", end - start);
            scheduleLog.setStatus(1);
            scheduleLog.setRemark("任务执行成功");
            scheduleLog.setTime((int) (end - start));
        } catch (Exception e) {
            e.fillInStackTrace();
            long end = System.currentTimeMillis();
            log.error(Arrays.toString(e.getStackTrace()));
            scheduleLog.setStatus(2);
            scheduleLog.setRemark(e.getMessage());
            scheduleLog.setTime((int) (end - start));
        } finally {
            scheduleLogMapper.insert(scheduleLog);
        }
    }

    @Scheduled(cron = "0 0 5 * * ?")
//    @Scheduled(cron = "0/5 0/1 * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void update() {
        String dictString = stringRedisTemplate.opsForValue().get(RedisConstant.DICT_KEY);
        File dictWords = FileUtil.newFile("/data/tmp.txt");
        FileUtil.writeUtf8String(dictString, dictWords);
        WordDictionary.getInstance().loadUserDict(dictWords.toPath(), StandardCharsets.UTF_8);
        JiebaSegmenter segmenter = new JiebaSegmenter();
        log.info("开始执行lowTag分析");
        long start = System.currentTimeMillis();
        ScheduleLog scheduleLog = new ScheduleLog();
        scheduleLog.setName("lowTag分析");
        scheduleLog.setType(3);
        try {
            for (int i = 1; i <= 4; i++) {
                Map<String, Object> map = new HashMap<>();
                map.put("high_tag_id", i);
                List<Blog> blogs = blogMapper.selectByMap(map);
                if (blogs == null || blogs.size() == 0) {
                    continue;
                }
                String redisKey = RedisConstant.LOW_TAG_FREQUENCY + i;
                stringRedisTemplate.delete(redisKey);
                for (Blog blog : blogs) {
                    List<String> lowTags = Arrays.asList(blog.getLowTags().split(SystemConstant.LOW_TAG_CONJUNCTION));
                    List<String> tagWords = new ArrayList<>();
                    lowTags.forEach(tag -> {
                        List<String> process = segmenter.sentenceProcess(tag);
                        tagWords.addAll(process);
                    });
                    executor.submit(new Thread(() -> {
                        tagWords.forEach(word -> {
                            stringRedisTemplate.opsForHash().increment(redisKey, word, 1);
                        });
                    }));
                }
            }
            long end = System.currentTimeMillis();
            log.info("lowTag分析，耗时：{}", end - start);
            scheduleLog.setStatus(1);
            scheduleLog.setRemark("任务执行成功");
            scheduleLog.setTime((int) (end - start));
        } catch (Exception e) {
            e.fillInStackTrace();
            long end = System.currentTimeMillis();
            log.error(Arrays.toString(e.getStackTrace()));
            scheduleLog.setStatus(2);
            scheduleLog.setRemark(e.getMessage());
            scheduleLog.setTime((int) (end - start));
        } finally {
            scheduleLogMapper.insert(scheduleLog);
            dictWords.delete();
        }
    }

    //    @Scheduled(cron = "0 0 5 * * ?")
    @Scheduled(cron = "0 0/5 * * * ?")
    @Transactional(rollbackFor = Exception.class)
    public void putViewTimeInToDb() {
        DateTime nowDate = DateUtil.date();
        long betweenDayStart = nowDate.between(DateUtil.beginOfDay(nowDate), DateUnit.SECOND);
        String pattern = ((betweenDayStart / (5 * 60)) - 1) + ":*";
        Set<String> keys = stringRedisTemplate.keys(BLOG_READ_KEY + pattern);
        if (keys == null) {
            return;
        }
        log.info("开始执行浏览量落表");
        long start = System.currentTimeMillis();
        keys.stream().forEach(each -> {
            log.info("浏览量落表,帖子key:{}", each);
            Long size = stringRedisTemplate.opsForSet().size(each);
            // 将key拆分
            String split = each.substring(each.lastIndexOf(':') + 1);
            // 根据blogId获取
            Blog blog = blogMapper.selectById(split);
            if (blog == null) {
                return;
            }
            blog.setViewTimes(blog.getViewTimes() + size.intValue());
            blogMapper.updateById(blog);
            stringRedisTemplate.delete(each);
        });
        long end = System.currentTimeMillis();
        log.info("浏览量落表，耗时：{}", end - start);
    }
}
