package com.qzx.xdupartner.controller;


import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.qzx.xdupartner.entity.Friend;
import com.qzx.xdupartner.entity.Message;
import com.qzx.xdupartner.entity.enumeration.MessageType;
import com.qzx.xdupartner.entity.vo.UserVo;
import com.qzx.xdupartner.exception.ApiException;
import com.qzx.xdupartner.service.FriendService;
import com.qzx.xdupartner.service.MessageService;
import com.qzx.xdupartner.service.UserService;
import com.qzx.xdupartner.util.UserHolder;

import cn.hutool.core.util.StrUtil;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author qzx
 * @since 2023-08-12
 */
@RestController
@RequestMapping("/friend")
public class FriendController {
    @Resource
    private UserService userService;
    @Resource
    private FriendService friendService;
    @Resource
    private MessageService messageService;

    @PostMapping(value = "/makeFriend", produces = "application/json;charset=utf-8")
    public String makeFriend(@Validated @RequestParam @NotNull Long friendId,
                             @Validated @RequestParam @NotNull String message) {
        boolean ifFriend = friendService.judgeIfFriend(friendId);
        if (ifFriend) {
            throw new ApiException("你们已经是好友啦，请勿重复添加哟！");
        } else {
            Message makeFriendMessage = new Message();
            makeFriendMessage.setType(MessageType.ADD_FRIEND.getCode());
            makeFriendMessage.setToId(friendId);
            makeFriendMessage.setContent(message);
            messageService.sendMessage(makeFriendMessage);
        }
        return "发送好友请求成功！";
    }

    @PostMapping(value = "/acceptFriend", produces = "application/json;charset=utf-8")
    public String acceptFriend(@Validated @RequestParam @NotNull Long friendId, @RequestParam String alterName) {
        boolean isFriend = friendService.judgeIfFriend(friendId);
        if (UserHolder.getUserId().equals(friendId) || isFriend) {
            throw new RuntimeException("你们之前就是好友了,请勿重复添加哟！");
        }
        boolean exist =
                friendService.count(friendService.query().eq("user_id", friendId)
                        .eq("friend_id", UserHolder.getUserId())) > 0;
        if (exist) {
            Friend entity = new Friend();
            entity.setUserId(UserHolder.getUserId());
            entity.setFriendId(friendId);
            entity.setAlterName(alterName);
            friendService.save(entity);
            return "你们已经是好友啦";
        } else {
            return "对方未曾发送好友申请";
        }
    }

    @GetMapping(value = "/allFriends", produces = "application/json;charset=utf-8")
    //查看自己的所有好友
    public List<UserVo> allFriends() {
        List<Friend> friends = friendService.query().eq("user_id", UserHolder.getUserId()).list();
        return friends.stream().map(friend -> {
            UserVo userVoById = userService.getUserVoById(friend.getFriendId());
            String alterName = friend.getAlterName();
            if (StrUtil.isNotBlank(alterName)) {
                userVoById.setNickName(alterName);
            }
            return userVoById;
        }).collect(Collectors.toList());
    }

    @PostMapping(value = "/changeFriendAlterName", produces = "application/json;charset=utf-8")
    public String changeFriendAlterName(@Validated @RequestParam @NotNull Long friendId,
                                        @RequestParam String alterName) {
        boolean update = friendService.update().eq("user_id", UserHolder.getUserId()).eq("friend_id", friendId).set(
                "alter_name", alterName).update();
        if (!update) {
            throw new RuntimeException("修改失败");
        }
        return "修改成功";
    }
}

