package com.exeplm.clients;

import com.exeplm.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("USERS-API")
public interface UserClient {

    @GetMapping("/getUser")
    User getUserName(@RequestParam("id") Integer id);

    @GetMapping("/getUserInfo")
     User getUserInfo(@RequestParam("token")String token);
}
