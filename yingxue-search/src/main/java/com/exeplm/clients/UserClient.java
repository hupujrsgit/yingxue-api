package com.exeplm.clients;

import com.exeplm.entity.User;
import com.exeplm.utils.TaoResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;

@FeignClient("USERS-API")
public interface UserClient {

    @GetMapping("/getUser")
    User getUserName(@RequestParam("id") Integer id);

    @GetMapping("/user")
     ResponseEntity<TaoResult> getUser(HttpServletRequest request);
}
