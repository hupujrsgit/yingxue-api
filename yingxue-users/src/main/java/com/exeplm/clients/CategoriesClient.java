package com.exeplm.clients;

import com.exeplm.utils.TaoResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("CATEGORIES-API")
public interface CategoriesClient {

    @GetMapping("/{id}")
    ResponseEntity<TaoResult> getCategory(@PathVariable("id")Integer id);
}
