package com.exeplm.service.impl;

import com.exeplm.clients.CategorieClient;
import com.exeplm.clients.UserClient;
import com.exeplm.dto.VideoDto;
import com.exeplm.entity.User;
import com.exeplm.entity.Video;
import com.exeplm.service.SearchService;
import com.exeplm.utils.RedisUtil;
import com.exeplm.utils.TaoResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private CategorieClient categorieClient;

    @Autowired
    private UserClient userClient;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public Map<String, Object> searchVideos(String q, Integer page, Integer per_page) throws IOException {
        int start = (page - 1) * per_page;
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder builder = new SearchSourceBuilder();
        //form 从第start开始，size 获取多少条数据，termQuery term查询
        builder.from(start).size(per_page).query(QueryBuilders.termQuery("title",q));
        //indices 搜索哪个索引， source 搜索参数
        searchRequest.indices("videos").source(builder);
        //search 调用search请求
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //获取返回值 SearchHits
        SearchHits hits = searchResponse.getHits();
        //数据总数
        long total = hits.getTotalHits().value;
        HashMap<String, Object> map = new HashMap<>();
        ArrayList<VideoDto> list = new ArrayList<>();
        if (total>0){
            SearchHit[] searchHits = hits.getHits();
            for (SearchHit searchHit : searchHits) {
                VideoDto videoDto = new VideoDto();
                String sourceAsString = searchHit.getSourceAsString();
                Map<String, Object> sourceAsMap = searchHit.getSourceAsMap();
                ObjectMapper mapper = new ObjectMapper();

                Video video = mapper.readValue(sourceAsString, Video.class);

                //复制值到videoDto
                BeanUtils.copyProperties(video,videoDto);
                //查找分类名称信息
                ResponseEntity<TaoResult> category = categorieClient.getCategory((Integer) sourceAsMap.get("categoryId"));
                Map<String,Object> data = (Map) category.getBody().getData();
                //赋值分类名称
                videoDto.setCategory((String) data.get("name"));
                //查找up主名称
                User userName = userClient.getUserName((Integer) sourceAsMap.get("uid"));
                //赋值up主名称
                videoDto.setUploader(userName.getName());
                //点赞redis接口
                videoDto.setLikes(0);
                Integer liked = (Integer) redisUtil.hget("videos_liked", "videos_liked_" + videoDto.getId());
                if (!ObjectUtils.isEmpty(liked)){
                    videoDto.setLikes(liked);
                }
                list.add(videoDto);
            }

        }
        map.put("total_count",total);
        map.put("items",list);
        return map;
    }
}
