package com.exeplm.mqProducer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class Producer {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void sendTextMessage(Map<String,String> map) {
        String receive = (String) rabbitTemplate.convertSendAndReceive("sendTextMessage", "sendTextMessage", map);
        log.info("消息队列返回信息:{}",receive);
    }
}
