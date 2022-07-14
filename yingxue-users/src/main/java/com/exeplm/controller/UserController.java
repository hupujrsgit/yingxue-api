package com.exeplm.controller;


import com.aliyun.oss.model.PutObjectResult;
import com.exeplm.annotation.RequiredToken;
import com.exeplm.clients.CategoriesClient;
import com.exeplm.clients.VideosClient;
import com.exeplm.dto.VideoDto;
import com.exeplm.entity.Played;
import com.exeplm.entity.User;
import com.exeplm.entity.Video;
import com.exeplm.exception.UserException;
import com.exeplm.modules.aop.OperationLogAnnotation;
import com.exeplm.service.PlayedService;
import com.exeplm.service.UserService;
import com.exeplm.utils.ImageUtils;
import com.exeplm.utils.OssUtils;
import com.exeplm.utils.RedisUtil;
import com.exeplm.utils.TaoResult;
import com.exeplm.vo.MsgVo;
import com.exeplm.vo.UserVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 用户(User)表控制层
 *
 * @author makejava
 * @since 2022-07-09 15:22:56
 */
@RestController
public class UserController {
    /**
     * 服务对象
     */
    @Resource
    private UserService userService;

    @Autowired
    private PlayedService playedService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private VideosClient videosClient;
    @Autowired
    private CategoriesClient categoriesClient;

    @PostMapping("/tokens")
    public ResponseEntity<TaoResult> tokens(@RequestBody MsgVo msgVo, HttpSession session){
        String phone = msgVo.getPhone();
        String captcha = msgVo.getCaptcha();

        if (!redisUtil.hasKey("phone_" + phone)){
            return new ResponseEntity<>(TaoResult.build(403,"验证码已过期"),HttpStatus.FORBIDDEN);
        }
        if (captcha.equals(redisUtil.get("phone_" + phone))){
            User user = userService.findByPhone(phone);
            if (ObjectUtils.isEmpty(user)){// 注册
                user = new User();//创建一个用户对象
                user.setName(phone);
                user.setCreatedAt(new Date());//设置创建时间
                user.setUpdatedAt(new Date());//设置更新时间
                user.setPhone(phone); //设置用户的手机号
                user.setIntro("");//设置简介为空
                //初始化默认头像
                user.setAvatar(ImageUtils.getPhoto());//随机初始化头像
                user.setPhoneLinked(1);//是否绑定手机
                user.setWechatLinked(0);//是否绑定微信
                user.setFollowersCount(0);//设置粉丝数
                user.setFollowingCount(0);//设置关注数
                user = userService.insert(user);//保存用户信息
            }
            String token = session.getId();
            redisUtil.set("session_"+token,user,30, TimeUnit.MINUTES);
            return new ResponseEntity<>(TaoResult.ok(token),HttpStatus.OK);
        }else {
            return new ResponseEntity<>(TaoResult.build(403,"验证码错误"),HttpStatus.FORBIDDEN);
        }
    }

    @GetMapping("/user")
    @RequiredToken
    @OperationLogAnnotation(operModul = "用户模块",operType = "获取",operDesc = "获取用户数据")
    public ResponseEntity<TaoResult> getUser(HttpServletRequest request){
        User user = (User) request.getAttribute("user");
        return new ResponseEntity<>(TaoResult.ok(user),HttpStatus.OK);
    }
    @DeleteMapping("/tokens")
    public ResponseEntity<TaoResult> lgout(String token){
        String key = "session_" + token;
        redisUtil.del(key);
        return  new ResponseEntity<>(TaoResult.ok(),HttpStatus.OK);
    }

    @PatchMapping("/user")
    @RequiredToken
    @OperationLogAnnotation(operModul = "用户模块",operType = "修改",operDesc = "修改用户数据")
    public ResponseEntity<TaoResult> updateUser(@RequestBody UserVo userVo,HttpServletRequest request){
        User user = (User) request.getAttribute("user");
        String token = (String) request.getAttribute("token");
        if (!ObjectUtils.isEmpty(userVo.getPhone())){
            String capcha = userVo.getCapcha();
            String phonekey = "phone_" + userVo.getPhone();
            if (!redisUtil.hasKey(phonekey)){
                throw new UserException("验证码已过期！");
            }else {
                String redisCapcha = (String) redisUtil.get(phonekey);
                if (!redisCapcha.equals(capcha)){
                    throw new UserException("验证码错误！");
                }
                user.setPhone(userVo.getPhone());
            }
        }
        user.setName(userVo.getName());
        user.setIntro(userVo.getIntro());

        userService.update(user);
        //更新redis
        redisUtil.set("session_"+token,user,30,TimeUnit.MINUTES);
        return new ResponseEntity<>(TaoResult.ok(user),HttpStatus.OK);
    }

    @PostMapping("/user/videos")
    @RequiredToken
    @OperationLogAnnotation(operModul = "用户模块",operType = "上传",operDesc = "用户上传视频")
    public ResponseEntity<TaoResult> uploadUserVideos(MultipartFile file,String title,String intro,Integer category_id,HttpServletRequest request) throws IOException {
        User user = (User) request.getAttribute("user");
        //获取文件后缀类型
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        UUID uuid = UUID.randomUUID();
        String fileName = uuid+"."+extension;
        //上传文件到OSS,返回视频文件地址
        String link = OssUtils.uploadOss(file.getInputStream(),fileName,"videos");
        //获取视频第一秒为封面
        String cover = link + "?x-oss-process=video/snapshot,t_1000,f_jpg,w_0,h_0,m_fast,ar_auto";


        Video video = new Video();
        video.setLink(link);
        video.setCover(cover);
        video.setTitle(title);
        video.setIntro(intro);
        video.setUid(user.getId());
        video.setCategoryId(category_id);
        //调用分类服务方法
        ResponseEntity<TaoResult> category = categoriesClient.getCategory(category_id);
        Map<String,Object> data = (Map) category.getBody().getData();
        //调用视频服务方法

        Video videodata = videosClient.uploadUserVideo(video);
        videodata.setCategory((String) data.get("name"));

        return new ResponseEntity<>(TaoResult.ok(videodata),HttpStatus.OK);
    }

    /**
     * 点击播放
     * @param vid
     * @param token
     * @return
     */
    @PutMapping("/user/played/{vid}")
    public ResponseEntity<TaoResult> playedVideo(@PathVariable("vid") Integer vid,String token){
        //增加播放量
        redisUtil.hincr("PLAYEDVIDEOS","PLAYED_VIDEOS_"+vid,1);
        User user = (User) redisUtil.get("session_" + token);
        if (!ObjectUtils.isEmpty(user)){
            Integer userId = user.getId();
            Played playedInfo = playedService.queryByUidAndVid(vid, userId);
            if (ObjectUtils.isEmpty(playedInfo)){
                playedInfo = new Played();
                playedInfo.setVideoId(vid);
                playedInfo.setUid(userId);
                Date date = new Date();
                playedInfo.setCreatedAt(date);
                playedInfo.setUpdatedAt(date);
                playedService.insert(playedInfo);
            }else {
                playedInfo.setUpdatedAt(new Date());
                playedService.update(playedInfo);
            }
        }
        return new ResponseEntity<>(TaoResult.build(204,null),HttpStatus.OK);
    }

    /**
     * 点赞
     * @param vid
     * @param token
     * @return
     */
    @PutMapping("/user/liked/{vid}")
    @OperationLogAnnotation(operModul = "用户模块",operType = "点赞",operDesc = "用户点赞")
    public ResponseEntity<TaoResult> playedVideoLike(@PathVariable("vid")Integer vid,String token){
        User user = (User) redisUtil.get("session_" + token);
        //记录进用户点赞视频列表
        redisUtil.sSet("user_liked_videos_"+user.getId(),vid);

        //记录视频列表有哪些用户喜欢
        redisUtil.sSet("videos_liked_user_"+vid,user.getId());
        //视频点赞次数+1
        redisUtil.hincr("videos_liked","videos_liked_"+vid,1);
        //删除用户不喜欢视频列表中的vid ，如果有的话
        redisUtil.setRemove("user_unliked_videos_"+user.getId(),vid);
        //删除视频列表中不喜欢的用户id，如果有的话
        redisUtil.setRemove("videos_unliked_user_"+vid,user.getId());

        return new ResponseEntity<>(TaoResult.ok("点赞成功"),HttpStatus.OK);
    }

    /**
     * 取消点赞
     * @param vid
     * @param token
     * @return
     */
    @DeleteMapping("/user/liked/{vid}")
    @OperationLogAnnotation(operModul = "用户模块",operType = "点赞",operDesc = "用户取消点赞")
    public ResponseEntity<TaoResult> playedVideoRmLike(@PathVariable("vid")Integer vid,String token){
        User user = (User) redisUtil.get("session_" + token);
        if (redisUtil.sHasKey("user_liked_videos_"+user.getId(),vid)){
            //视频点赞次数-1
            redisUtil.hdecr("videos_liked","videos_liked_"+vid,1);
            //移除用户对这个视频的点赞记录
            redisUtil.setRemove("user_liked_videos_"+user.getId(),vid);
            //移除视频中点赞用户列表的id
            redisUtil.setRemove("videos_liked_user_"+vid,user.getId());

        }

        return new ResponseEntity<>(TaoResult.ok("取消点赞成功"),HttpStatus.OK);
    }

    /**
     * 不喜欢
     * @param vid
     * @param token
     * @return
     */
    @PutMapping("/user/disliked/{vid}")
    public ResponseEntity<TaoResult> playedVideoUnLike(@PathVariable("vid")Integer vid,String token){
        User user = (User) redisUtil.get("session_" + token);
        //记录到 该视频的不喜欢用户列表
        redisUtil.sSet("videos_unliked_user_"+vid,user.getId());
        //记录到 该用户的不喜欢视频列表
        redisUtil.sSet("user_unliked_videos_"+user.getId(),vid);
        if (redisUtil.sHasKey("user_liked_videos_"+user.getId(),vid)){//如果之前这个用户点赞过
            //移除用户点赞视频列表记录
            redisUtil.setRemove("user_liked_videos_"+user.getId(),vid);
            //移除视频点赞用户列表记录
            redisUtil.setRemove("videos_liked_user_"+vid,user.getId());
            //视频点赞次数-1
            redisUtil.hdecr("videos_liked","videos_liked_"+vid,1);
        }

        return new ResponseEntity<>(TaoResult.ok("不喜欢视频成功"),HttpStatus.OK);
    }

    /**
     * 收藏
     * @param vid
     * @param token
     * @return
     */
    @PutMapping("/user/favoites/{vid}")
    @OperationLogAnnotation(operModul = "用户模块",operType = "收藏",operDesc = "用户收藏")
    public ResponseEntity<TaoResult> favoites(@PathVariable("vid") Integer vid,String token){
        User user = (User) redisUtil.get("session_" + token);
        //记录用户收藏视频记录
        redisUtil.sSet("user_favoites_videos_"+user.getId(),vid);
        //记录视频收藏用户列表
        redisUtil.sSet("videos_favoites_user"+vid,user.getId());
        //记录视频收藏次数+1
        redisUtil.hincr("videos_favoites","videos_favoites"+vid,1);

        return new ResponseEntity<>(TaoResult.ok("收藏成功"),HttpStatus.OK);
    }

    /**
     * 取消收藏
     * @param vid
     * @param token
     * @return
     */
    @DeleteMapping("/user/favoites/{vid}")
    @OperationLogAnnotation(operModul = "用户模块",operType = "收藏",operDesc = "用户取消收藏")
    public ResponseEntity<TaoResult> unfavoites(@PathVariable("vid") Integer vid,String token){
        User user = (User) redisUtil.get("session_" + token);
        if (redisUtil.sHasKey("user_favoites_videos_"+user.getId(),vid)){
            //取消用户收藏视频记录
            redisUtil.setRemove("user_favoites_videos_"+user.getId(),vid);
            //取消视频收藏用户列表
            redisUtil.setRemove("videos_favoites_user"+vid,user.getId());
            redisUtil.hdecr("videos_favoites","videos_favoites"+vid,1);
        }

        return new ResponseEntity<>(TaoResult.ok("取消收藏成功"),HttpStatus.OK);
    }


    /**
     * 获取收藏记录列表
     * @param page
     * @param per_page
     * @param request
     * @return
     */
    @GetMapping("/user/favoites")
    @RequiredToken
    @OperationLogAnnotation(operModul = "用户模块",operType = "收藏",operDesc = "获取用户收藏记录")
    public ResponseEntity<TaoResult> userFavoites(@RequestParam(value = "page",defaultValue = "1") Integer page,@RequestParam(value = "per_page",defaultValue = "10") Integer per_page,HttpServletRequest request){
        User user = (User) request.getAttribute("user");

        ArrayList<VideoDto> videoDtos = new ArrayList<>();
        Set<Object> videosIds = redisUtil.sGet("user_favoites_videos_" + user.getId());
        for (Object videosId : videosIds) {
            VideoDto video = videosClient.getVideo(Integer.parseInt(videosId.toString()));
            videoDtos.add(video);
        }

        return new ResponseEntity<>(TaoResult.ok(videoDtos),HttpStatus.OK);
    }

    /**
     * 获取播放记录列表
     * @param page
     * @param per_page
     * @param request
     * @return
     */
    @GetMapping("/user/played")
    @RequiredToken
    @OperationLogAnnotation(operModul = "用户模块",operType = "播放",operDesc = "用户播放记录")
    public ResponseEntity<TaoResult> userPlayed(@RequestParam(value = "page",defaultValue = "1") Integer page,@RequestParam(value = "per_page",defaultValue = "10") Integer per_page,HttpServletRequest request){
        User user = (User) request.getAttribute("user");

        List<Played> playedRecord = playedService.queryByUid(page, per_page, user.getId());
        ArrayList<VideoDto> videoDtos = new ArrayList<>();
        for (Played played : playedRecord) {
            VideoDto video = videosClient.getVideo(played.getVideoId());
            videoDtos.add(video);
        }
        return new ResponseEntity<>(TaoResult.ok(videoDtos),HttpStatus.OK);
    }

    @GetMapping("/getUser")
    public User getUserName(Integer id){
        User user = userService.queryById(id);
        return user;
    }

    @GetMapping("/getUserInfo")
    public User getUserInfo(String token){
        User user = (User) redisUtil.get("session_" + token);
        return  user;
    }


}

