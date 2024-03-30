package com.qzx.xdupartner.controller;


import cn.hutool.core.util.StrUtil;
import com.qzx.xdupartner.entity.Friend;
import com.qzx.xdupartner.entity.Message;
import com.qzx.xdupartner.entity.enumeration.MessageType;
import com.qzx.xdupartner.entity.vo.ResultCode;
import com.qzx.xdupartner.entity.vo.ResultVo;
import com.qzx.xdupartner.entity.vo.UserVo;
import com.qzx.xdupartner.exception.ApiException;
import com.qzx.xdupartner.service.FriendService;
import com.qzx.xdupartner.service.MessageService;
import com.qzx.xdupartner.service.UserService;
import com.qzx.xdupartner.util.UserHolder;
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
    public ResultVo<String> makeFriend(@Validated @RequestParam @NotNull Long friendId,
                             @Validated @RequestParam @NotNull String message) {
        boolean ifFriend = friendService.judgeIfFriend(friendId);
        if (ifFriend) {
            return new ResultVo<>(ResultCode.VALIDATE_ERROR, "你们已经是好友啦，请勿重复添加哟！");
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
                return new ResultVo<>(ResultCode.VALIDATE_ERROR, "你已发送过好友请求");
            }
        }
        return new ResultVo<>(ResultCode.SUCCESS, "发送好友请求成功！");
    }

    @PostMapping(value = "/acceptFriend", produces = "application/json;charset=utf-8")
    public ResultVo<String> acceptFriend(@Validated @RequestParam @NotNull Long friendId, @RequestParam String alterName) {
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
            return new ResultVo<>(ResultCode.SUCCESS, "你们已经是好友啦");
        } else {
            return new ResultVo<>(ResultCode.VALIDATE_ERROR, "对方未曾发送好友申请");
        }
    }

    @GetMapping(value = "/allFriends", produces = "application/json;charset=utf-8")
    //查看自己的所有好友
    public ResultVo<List<UserVo>> allFriends() {
        List<Friend> friends = friendService.query().eq("user_id", UserHolder.getUserId()).list();
        return new ResultVo<>(ResultCode.SUCCESS, friends.stream().filter(f -> friendService.judgeIfFriend(f.getFriendId())).map(friend -> {
            UserVo userVoById = userService.getUserVoById(friend.getFriendId());
            String alterName = friend.getAlterName();
            if (StrUtil.isNotBlank(alterName)) {
                userVoById.setNickName(alterName);
            }
            return userVoById;
        }).collect(Collectors.toList()));
    }

    @PostMapping(value = "/changeFriendAlterName", produces = "application/json;charset=utf-8")
    public ResultVo<String> changeFriendAlterName(@Validated @RequestParam @NotNull Long friendId,
                                        @RequestParam String alterName) {
        if ("".equals(alterName)) {
            return new ResultVo<>(ResultCode.VALIDATE_ERROR,"备注不能为空");
        }
        boolean update = friendService.update().eq("user_id", UserHolder.getUserId()).eq("friend_id", friendId).set(
                "alter_name", alterName).update();
        return new ResultVo<>(ResultCode.SUCCESS, "修改成功");
    }
}

