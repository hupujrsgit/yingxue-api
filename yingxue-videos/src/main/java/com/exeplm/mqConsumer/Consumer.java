package com.exeplm.mqConsumer;



import com.exeplm.entity.Video;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class Consumer {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue, // 创建临时队列
                    key = {"sendVideosES"}, // 路由key
                    exchange = @Exchange(type = "direct", name = "sendVideosES")
            )})
    public String sendVideosES(String videoJSON) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Video video = mapper.readValue(videoJSON, Video.class);
        //创建索引对象
//        IndexRequest indexRequest = new IndexRequest("videos", "videos", video.getId().toString());

        IndexRequest indexRequest = new IndexRequest("videos");
        indexRequest.id(video.getId().toString());
        //创建文档内容
        indexRequest.source(videoJSON, XContentType.JSON);
        //将文档录入ES中
        IndexResponse index = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);

        log.info("录入ES状态:{}",index.status());

        return "消息消费结束";
    }

}
