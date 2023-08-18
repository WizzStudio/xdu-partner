package com.qzx.xdupartner.schedule;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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

@Slf4j
@Component
public class ScheduleMission {
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);
    @Resource
    private ScheduleLogMapper scheduleLogMapper;
    @Resource
    private BlogMapper blogMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @PostConstruct
    public void init() throws IOException {
        update();
    }

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
                Long viewTimes = Long.valueOf(stringRedisTemplate.opsForValue().get(key));
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
            log.error(Arrays.toString(e.getStackTrace()));
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
                if (entries.isEmpty()) continue;
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
        FileUtil.writeUtf8String(""+dictString, dictWords);
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
}
