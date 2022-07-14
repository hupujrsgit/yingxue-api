package com.exeplm.clients;

import com.exeplm.dto.VideoDto;
import com.exeplm.entity.Video;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("VIDEOS-API")
public interface VideosClient {

    @PostMapping("/videoService/uploadUserVideo")
    Video uploadUserVideo(@RequestBody Video video);


    @GetMapping("/videoService/getVideo")
     VideoDto getVideo(@RequestParam("vid") Integer vid);
}
