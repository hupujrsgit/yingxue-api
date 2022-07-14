package com.exeplm.controller;

import com.exeplm.modules.aop.OperationLogAnnotation;
import com.exeplm.mqProducer.Producer;
import com.exeplm.utils.RedisUtil;
import com.exeplm.utils.TaoResult;
import com.exeplm.vo.SmsVO;
import org.apache.commons.lang.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

@RestController
public class SmsController {

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private Producer producer;

    @PostMapping("/captchas")
    @OperationLogAnnotation(operModul = "短信模块",operType = "短信",operDesc = "发送短信")
    public ResponseEntity<TaoResult> captchas(@RequestBody SmsVO smsVO){
        String phone = smsVO.getPhone();
        String timeoutKey = "timeout_" + phone;
        if (redisUtil.hasKey(timeoutKey)) {
            throw new RuntimeException("提示: 不允许重复发送!");
        }
        //生成四位随机数
        String code = RandomStringUtils.randomNumeric(4);
        HashMap<String, String> map = new HashMap<>();
        map.put("code",code);
        map.put("phone",phone);
        //mq异步发送短信
        producer.sendTextMessage(map);

        return new ResponseEntity<>(TaoResult.ok("短信发送成功"), HttpStatus.OK);
    }

}
