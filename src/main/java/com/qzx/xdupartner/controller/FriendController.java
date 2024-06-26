package com.qzx.xdupartner.controller;


import cn.hutool.core.util.StrUtil;
import com.qzx.xdupartner.entity.Friend;
import com.qzx.xdupartner.entity.Message;
import com.qzx.xdupartner.entity.enumeration.MessageType;
import com.qzx.xdupartner.entity.vo.R;
import com.qzx.xdupartner.entity.vo.ResultCode;
import com.qzx.xdupartner.entity.vo.UserVo;
import com.qzx.xdupartner.service.FriendService;
import com.qzx.xdupartner.service.MessageService;
import com.qzx.xdupartner.service.UserService;
import com.qzx.xdupartner.util.RUtil;
import com.qzx.xdupartner.util.UserHolder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author qzx
 * @since 2023-08-12
 */
@Api
@RestController
@RequestMapping("/friend")
@ApiOperation("好友控制层")
public class FriendController {
    @Resource
    private UserService userService;
    @Resource
    private FriendService friendService;
    @Resource
    private MessageService messageService;


    @ApiOperation("加好友")
    @PostMapping(value = "/make", produces = "application/json;charset=utf-8")
    public R<String> makeFriendV2(@Validated @RequestParam @NotNull Long friendId,
                                  @Validated @RequestParam @NotNull String message) {
        boolean ifFriend = friendService.judgeIfFriend(friendId);
        if (ifFriend) {
            return new R<>(ResultCode.VALIDATE_ERROR, "你们已经是好友啦，请勿重复添加哟！");
        } else {
            Integer count =
                    friendService.query().eq("user_id", UserHolder.getUserId()).eq("friend_id", friendId).count();
            if ((count == 0)) {
                Message makeFriendMessage = new Message();
                makeFriendMessage.setType(MessageType.ADD_FRIEND.getCode());
                makeFriendMessage.setToId(friendId);
                makeFriendMessage.setContent(message);
                messageService.sendMessage(makeFriendMessage);
                Friend friend = new Friend();
                friend.setUserId(UserHolder.getUserId());
                friend.setFriendId(friendId);
                friendService.save(friend);
            } else {
                return new R<>(ResultCode.VALIDATE_ERROR, "你已发送过好友请求");
            }
        }
        return RUtil.success("发送好友请求成功！");
    }


    @ApiOperation("更改好友备注")
    @PostMapping(value = "/alter/name", produces = "application/json;charset=utf-8")
    public R<String> changeFriendAlterNameV2(@Validated @RequestParam @NotNull Long friendId,
                                             @RequestParam String alterName) {
        if ("".equals(alterName)) {
            return new R<>(ResultCode.VALIDATE_ERROR, "备注不能为空");
        }
        boolean update = friendService.update().eq("user_id", UserHolder.getUserId()).eq("friend_id", friendId).set(
                "alter_name", alterName).update();
        return RUtil.success("修改成功");
    }

    @ApiOperation("查看自己的所有好友")
    @GetMapping(value = "/all", produces = "application/json;charset=utf-8")
    public R<List<UserVo>> allFriendsV2() {
        List<Friend> friends = friendService.query().eq("user_id", UserHolder.getUserId()).list();
        return RUtil.success(
                friends.stream().filter(f -> friendService.judgeIfFriend(f.getFriendId())).map(friend -> {
                    UserVo userVoById = userService.getUserVoById(friend.getFriendId());
                    String alterName = friend.getAlterName();
                    if (StrUtil.isNotBlank(alterName)) {
                        userVoById.setNickName(alterName);
                    }
                    return userVoById;
                }).collect(Collectors.toList()));
    }

    @ApiOperation("接受好友请求")
    @PostMapping(value = "/accept", produces = "application/json;charset=utf-8")
    public R<String> acceptFriendV2(@Validated @RequestParam @NotNull Long friendId, @RequestParam String alterName) {
        boolean isFriend = friendService.judgeIfFriend(friendId);
        if (UserHolder.getUserId().equals(friendId) || isFriend) {
            throw new RuntimeException("你们之前就是好友了,请勿重复添加哟！");
        }
        boolean exist =
                friendService.query().eq("user_id", friendId)
                        .eq("friend_id", UserHolder.getUserId()).count() > 0;
        if (exist) {
            Friend entity = new Friend();
            entity.setUserId(UserHolder.getUserId());
            entity.setFriendId(friendId);
            entity.setAlterName(alterName);
            friendService.save(entity);
            return RUtil.success("你们已经是好友啦");
        } else {
            return new R<>(ResultCode.VALIDATE_ERROR, "对方未曾发送好友申请");
        }
    }

    @Deprecated
    @ApiOperation("查看自己的所有好友")
    @GetMapping(value = "/allFriends", produces = "application/json;charset=utf-8")
    public R<List<UserVo>> allFriends() {
        List<Friend> friends = friendService.query().eq("user_id", UserHolder.getUserId()).list();
        return RUtil.success(
                friends.stream().filter(f -> friendService.judgeIfFriend(f.getFriendId())).map(friend -> {
                    UserVo userVoById = userService.getUserVoById(friend.getFriendId());
                    String alterName = friend.getAlterName();
                    if (StrUtil.isNotBlank(alterName)) {
                        userVoById.setNickName(alterName);
                    }
                    return userVoById;
                }).collect(Collectors.toList()));
    }


    @Deprecated
    @ApiOperation("更改好友备注")
    @PostMapping(value = "/changeFriendAlterName", produces = "application/json;charset=utf-8")
    public R<String> changeFriendAlterName(@Validated @RequestParam @NotNull Long friendId,
                                           @RequestParam String alterName) {
        if ("".equals(alterName)) {
            return new R<>(ResultCode.VALIDATE_ERROR, "备注不能为空");
        }
        boolean update = friendService.update().eq("user_id", UserHolder.getUserId()).eq("friend_id", friendId).set(
                "alter_name", alterName).update();
        return RUtil.success("修改成功");
    }

    @Deprecated
    @ApiOperation("接受好友请求")
    @PostMapping(value = "/acceptFriend", produces = "application/json;charset=utf-8")
    public R<String> acceptFriend(@Validated @RequestParam @NotNull Long friendId, @RequestParam String alterName) {
        boolean isFriend = friendService.judgeIfFriend(friendId);
        if (UserHolder.getUserId().equals(friendId) || isFriend) {
            throw new RuntimeException("你们之前就是好友了,请勿重复添加哟！");
        }
        boolean exist =
                friendService.query().eq("user_id", friendId)
                        .eq("friend_id", UserHolder.getUserId()).count() > 0;
        if (exist) {
            Friend entity = new Friend();
            entity.setUserId(UserHolder.getUserId());
            entity.setFriendId(friendId);
            entity.setAlterName(alterName);
            friendService.save(entity);
            return RUtil.success("你们已经是好友啦");
        } else {
            return new R<>(ResultCode.VALIDATE_ERROR, "对方未曾发送好友申请");
        }
    }

    @Deprecated
    @ApiOperation("加好友")
    @PostMapping(value = "/makeFriend", produces = "application/json;charset=utf-8")
    public R<String> makeFriend(@Validated @RequestParam @NotNull Long friendId,
                                @Validated @RequestParam @NotNull String message) {
        boolean ifFriend = friendService.judgeIfFriend(friendId);
        if (ifFriend) {
            return new R<>(ResultCode.VALIDATE_ERROR, "你们已经是好友啦，请勿重复添加哟！");
        } else {
            Integer count =
                    friendService.query().eq("user_id", UserHolder.getUserId()).eq("friend_id", friendId).count();
            if ((count == 0)) {
                Message makeFriendMessage = new Message();
                makeFriendMessage.setType(MessageType.ADD_FRIEND.getCode());
                makeFriendMessage.setToId(friendId);
                makeFriendMessage.setContent(message);
                messageService.sendMessage(makeFriendMessage);
                Friend friend = new Friend();
                friend.setUserId(UserHolder.getUserId());
                friend.setFriendId(friendId);
                friendService.save(friend);
            } else {
                return new R<>(ResultCode.VALIDATE_ERROR, "你已发送过好友请求");
            }
        }
        return RUtil.success("发送好友请求成功！");
    }
}

