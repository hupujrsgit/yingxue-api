package com.exeplm.controller;

import com.exeplm.modules.aop.OperationLogAnnotation;
import com.exeplm.service.SearchService;
import com.exeplm.utils.TaoResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
public class SearchController {
    @Autowired
    private SearchService searchService;

    @OperationLogAnnotation(operModul = "查询模块",operType = "查询",operDesc = "视频标题查询")
    @GetMapping("/search/videos")
    public ResponseEntity<TaoResult> searchVideos(String q, @RequestParam(value = "page",defaultValue = "1") Integer page, @RequestParam(value = "per_page",defaultValue = "10") Integer per_page) throws IOException {
        Map<String, Object> map = searchService.searchVideos(q, page, per_page);
        return new ResponseEntity<>(TaoResult.ok(map), HttpStatus.OK);
    }
}
