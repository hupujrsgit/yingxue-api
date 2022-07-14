package com.exeplm.mqConsumer;


import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.exeplm.utils.AliyunSmsUtils;
import com.exeplm.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class Consumer {

    @Autowired
    private RedisUtil redisUtil;

    @RabbitListener(bindings = {
            @QueueBinding(
                    value = @Queue, // 创建临时队列
                    key = {"sendTextMessage"}, // 路由key
                    exchange = @Exchange(type = "direct", name = "sendTextMessage")
            )})
    public String sendTextMessage(Map<String,String> map)  {
        String code = map.get("code");
        String phone = map.get("phone");


        try {
            SendSmsResponse sendSmsResponse = AliyunSmsUtils.sendSms(phone, code);

            if(sendSmsResponse!=null&&sendSmsResponse.body.code.equals("OK")){
                log.info("短信验证码:{}",code);
                log.info("接收短信手机为:{}",phone);
                String phoneKey = "phone_" + phone;
                redisUtil.set(phoneKey,code,60, TimeUnit.MINUTES);
                String timeoutphoneKey = "timeout_" + phone;
                redisUtil.set(timeoutphoneKey,"true",60,TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            log.error("短信发送失败");
            throw new RuntimeException(e);
        }

        return "发送成功";
    }

}
