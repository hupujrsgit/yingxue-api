package com.exeplm.controller;

import com.exeplm.dto.CommentDto;
import com.exeplm.dto.VideoDetailsDto;
import com.exeplm.dto.VideoDto;
import com.exeplm.entity.Comment;
import com.exeplm.entity.User;
import com.exeplm.entity.Video;
import com.exeplm.service.CommentService;
import com.exeplm.service.VideoService;
import com.exeplm.utils.RedisUtil;
import com.exeplm.utils.TaoResult;
import com.exeplm.vo.CommentsVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.websocket.server.PathParam;
import java.util.Date;
import java.util.List;

/**
 * 视频(Video)表控制层
 *
 * @author makejava
 * @since 2022-07-10 21:24:39
 */
@RestController
public class VideoController {
    /**
     * 服务对象
     */
    @Resource
    private VideoService videoService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private RedisUtil redisUtil;

    @GetMapping("/recommends")
    public ResponseEntity<TaoResult> recommends(@RequestParam(value = "page",defaultValue = "1") Integer page,@RequestParam(value = "per_page",defaultValue = "10") Integer per_page){
        List<VideoDto> videoDtos = videoService.queryByPage(page, per_page);
        return new ResponseEntity<>(TaoResult.ok(videoDtos), HttpStatus.OK);
    }

    @GetMapping("/videos/{vid}")
    public ResponseEntity<TaoResult> getVideos(@PathVariable("vid") Integer vid,String token){
        VideoDetailsDto videosDetails = videoService.getVideosDetails(vid, token);
        return new ResponseEntity<>(TaoResult.ok(videosDetails),HttpStatus.OK);
    }

    /**
     * 评论记录
     * @param vid
     * @param page
     * @param per_page
     * @return
     */
    @GetMapping("/videos/{vid}/comments")
    public ResponseEntity<TaoResult> getComments(@PathVariable("vid") Integer vid,@RequestParam(value = "page",defaultValue = "1") Integer page,@RequestParam(value = "per_page",defaultValue = "10") Integer per_page){
        List<CommentDto> commentDtos = commentService.queryByVidPage(vid, page, per_page);
        return new ResponseEntity<>(TaoResult.ok(commentDtos),HttpStatus.OK);
    }

    @PostMapping("/videos/{vid}/comments")
    public ResponseEntity<TaoResult> addComments(@RequestBody CommentsVo commentsVo, String token,@PathVariable("vid") Integer vid){
        User user = (User) redisUtil.get("session_" + token);
        Comment comment = new Comment();
        BeanUtils.copyProperties(commentsVo,comment);
        comment.setUid(user.getId());
        comment.setVideoId(vid);
        Date date = new Date();
        comment.setCreatedAt(date);
        comment.setUpdatedAt(date);
        commentService.insert(comment);
        return new ResponseEntity<>(TaoResult.ok("添加评论成功"),HttpStatus.OK);
    }
}

