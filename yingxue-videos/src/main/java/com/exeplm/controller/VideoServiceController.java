package com.exeplm.controller;

import com.exeplm.clients.CategorieClient;
import com.exeplm.clients.UserClient;
import com.exeplm.dto.VideoDto;
import com.exeplm.entity.User;
import com.exeplm.entity.Video;
import com.exeplm.mqProducer.Producer;
import com.exeplm.service.VideoService;
import com.exeplm.utils.RedisUtil;
import com.exeplm.utils.TaoResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;

@RestController
public class VideoServiceController {

    /**
     * 服务对象
     */
    @Resource
    private VideoService videoService;
    @Autowired
    private Producer producer;

    @Autowired
    private UserClient userClient;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private CategorieClient categorieClient;

    @PostMapping("/videoService/uploadUserVideo")
    public Video uploadUserVideo(@RequestBody Video video) throws JsonProcessingException {
        Date date = new Date();
        video.setCreatedAt(date);
        video.setUpdatedAt(date);
        //添加视频数据
        videoService.insert(video);
        ObjectMapper mapper = new ObjectMapper();
        String videoJSON = mapper.writeValueAsString(video);
        //mq异步调用
        producer.sendVideosES(videoJSON);
        return  video;
    }
    @GetMapping("/videoService/getVideo")
    public VideoDto getVideo(Integer vid){
        VideoDto videoDto = new VideoDto();
        Video video = videoService.queryById(vid);
        BeanUtils.copyProperties(video,videoDto);
        User user = userClient.getUserName(video.getUid());
        videoDto.setUploader(user.getName());
        ResponseEntity<TaoResult> responseEntity = categorieClient.getCategory(video.getCategoryId());
        Map<String,Object> data = (Map<String, Object>) responseEntity.getBody().getData();
        videoDto.setCategory((String) data.get("name"));
        //先默认0
        videoDto.setLikes(0);
        //获取redis中视频的点赞数
        Integer liked = (Integer) redisUtil.hget("videos_liked", "videos_liked_" + videoDto.getId());
        if (!ObjectUtils.isEmpty(liked)){
            videoDto.setLikes(liked);
        }
        return videoDto;
    }

}
