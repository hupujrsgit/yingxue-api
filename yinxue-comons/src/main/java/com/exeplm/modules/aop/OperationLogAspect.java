package com.exeplm.modules.aop;


import com.exeplm.entity.OperationLog;
import com.exeplm.utils.IpUtil;
import com.exeplm.utils.TaoResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * @author lyz
 * @title: OperationAspect
 * @projectName springcloud
 * @date 2020/9/23
 * @description: 操作日志切面处理类
 */
@Aspect
@Component
@Slf4j
public class OperationLogAspect {
//    @Autowired
//    OperationLogMapper logDao;
//
//    @Autowired
//    private Producer producer;


    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 设置操作日志切入点   在注解的位置切入代码
     */
    @Pointcut("@annotation(com.exeplm.modules.aop.OperationLogAnnotation)")
    public void operLogPoinCut() {
    }

    /**
     * 记录操作日志
     * @param joinPoint 方法的执行点
     * @param result  方法返回值
     * @throws Throwable
     */
    @AfterReturning(returning = "result", value = "operLogPoinCut()")
    public void saveOperLog(JoinPoint joinPoint, Object result) throws Throwable {
        // 获取RequestAttributes
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        // 从获取RequestAttributes中获取HttpServletRequest的信息
        HttpServletRequest request = (HttpServletRequest) requestAttributes.resolveReference(RequestAttributes.REFERENCE_REQUEST);
        try {
            //将返回值转换成map集合
            ResponseEntity<TaoResult> responseEntity = (ResponseEntity<TaoResult>) result;
            String msg =  responseEntity.getBody().getMsg();
//        Map<String, String> map = (Map<String, String>) result;
            OperationLog operationLog = new OperationLog();
            // 从切面织入点处通过反射机制获取织入点处的方法
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            //获取切入点所在的方法
            Method method = signature.getMethod();
            //获取操作
            OperationLogAnnotation annotation = method.getAnnotation(OperationLogAnnotation.class);
            if (annotation != null) {
                operationLog.setModel(annotation.operModul());
                operationLog.setType(annotation.operType());
                operationLog.setDesciption(annotation.operDesc());
            }
            //操作时间
            operationLog.setOperationTime(sdf.format(new Date()));
            //操作用户
//            SysUser sysUser = (SysUser) SecurityUtils.getSubject().getPrincipal();
//            if(sysUser != null){
//                operationLog.setSysUserId(sysUser.getId());
//            }
            //操作IP
            operationLog.setIp(IpUtil.getIpAddr(request));
            //返回值信息

            ObjectMapper mapper = new ObjectMapper();
            operationLog.setResult(msg);
            String writeValueAsString = mapper.writeValueAsString(operationLog);
            //添加日志，自动打印到es
            log.info(writeValueAsString);
            //保存日志
//            logDao.save(operationLog);
            //发送mq
//            producer.saveLogMQ(operationLog);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}



